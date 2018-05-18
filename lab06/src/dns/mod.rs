/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab06, DNS
 */
pub mod util;

use std::io;
use std::net::{SocketAddr, UdpSocket};

const UDP_BYTES: usize = 512;
const DNS_ANSWER_EXPECTED_SIZE: usize = 16;

// Utility for setup:
fn setup_request(
    request_bytes: &mut [u8], domain: &str
) -> Result<usize, util::DnsError> {
    // Transaction id will remain 0x0000

    // Set up transaction standard recursion query flags (0x0100):
    request_bytes[2] = 1;

    // Set up question count (0x0001):
    request_bytes[5] = 1;

    let mut i = 12;

    // Split the string by `.`:
    for label in domain.split('.') {
        request_bytes[i] = label.bytes().count() as u8;
        i += 1;

        for label_byte in label.bytes() {
            request_bytes[i] = label_byte;
            i += 1;
        }
    }

    // Set type A
    i += 2; // Count in the trailing \0
    request_bytes[i] = 1;
    // Set class IN
    i += 2;
    request_bytes[i] = 1;

    // Return length of the buffer data:
    Ok(i + 1)
}

// Check dns address:
fn check_response_address(
    result_addr: &SocketAddr, orig_addr: &SocketAddr
) -> Result<(), util::DnsError> {
    if result_addr != orig_addr {
        return Err(util::DnsError::new(
            "Original address is not equal to result addres!"
        ));
    }

    Ok(())
}

// Process dns response:
fn process_response(
    resp_size: usize,
    resp_bytes: &[u8; UDP_BYTES],
    req_size: usize,
    orig_address: &str,
) -> Result<Vec<util::Answer>, util::DnsError> {
    if resp_size < req_size {
        return Err(util::DnsError::new(
            "Response size must be greater than request size!",
        ));
    }

    // Start checking bytes
    let mut i = req_size;
    let mut answers = vec![];

    while resp_size - i >= DNS_ANSWER_EXPECTED_SIZE {
        if resp_bytes[i] != 0xc0 || resp_bytes[i + 1] != 0x0c {
            return Err(util::DnsError::new(
                "Invalid or no backreference in answer!",
            ));
        }
        i += 2;

        if resp_bytes[i] != 0x00 || resp_bytes[i + 1] != 0x01 {
            return Err(util::DnsError::new("Invalid type in answer!"));
        }
        i += 2;

        if resp_bytes[i] != 0x00 || resp_bytes[i + 1] != 0x01 {
            return Err(util::DnsError::new("Invalid class in answer!"));
        }
        i += 7; // 2 + 4 + 1 we jump the TTL field and one of the data
                // length fields (We don't expect data to be more than 256!)

        let data_length = resp_bytes[i] as usize;
        i += 1;

        if data_length != 4 {
            return Err(util::DnsError::new("Data length should be 4!"));
        }

        let answer_address = format!(
            "{}.{}.{}.{}",
            resp_bytes[i],
            resp_bytes[i + 1],
            resp_bytes[i + 2],
            resp_bytes[i + 3]
        );

        i += 4;

        answers.push(util::Answer::new(orig_address, &answer_address));
    }

    Ok(answers)
}

// Lookup logic:
fn dns_do_lookup(
    bind_addr: &str,
    server_addr: &SocketAddr,
    req_bytes: &[u8; UDP_BYTES],
    req_bytes_count: usize,
) -> io::Result<(usize, SocketAddr, [u8; UDP_BYTES])> {
    // Create UDP connection:
    let socket = UdpSocket::bind(bind_addr)?;

    // Send request:
    socket.send_to(&req_bytes[..req_bytes_count], server_addr)?;

    // Receive answer:
    let mut resp_bytes = [0; UDP_BYTES];
    let (recv_bc, src) = socket.recv_from(&mut resp_bytes)?;

    Ok((recv_bc, src, resp_bytes))
}

// Lookup wrapper:
pub fn dns_lookup(
    bind_addr: &str,
    server_addr: &SocketAddr,
    name_query: &str,
) -> Result<Vec<util::Answer>, util::DnsError> {
    let mut request_bytes = [0; UDP_BYTES];

    // Set up request:
    let req_size = setup_request(&mut request_bytes, name_query)?;

    let result = match dns_do_lookup(bind_addr, server_addr, &request_bytes, req_size) {
        Ok(result) => result,
        Err(ioerr) => {
            return Err(util::DnsError::new(&format!(
                "DNS query failed with underlying error: {}",
                ioerr
            )))
        }
    };

    check_response_address(server_addr, &result.1)?;

    process_response(result.0, &result.2, req_size, name_query)
}
