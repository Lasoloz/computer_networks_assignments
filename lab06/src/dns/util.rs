/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab06, DNS
 */
use std::error::Error;
use std::fmt;

// Error type for dns lookup
#[derive(Debug)]
pub struct DnsError {
    msg: String,
}

impl DnsError {
    pub fn new(msg: &str) -> DnsError {
        DnsError {
            msg: msg.to_owned(),
        }
    }
}

impl Error for DnsError {
    fn description(&self) -> &str {
        self.msg.as_str()
    }
}

impl fmt::Display for DnsError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "[DnsError] {}", self.msg)
    }
}

// Answer type
// TODO: Document it!
pub struct Answer {
    name: String,
    address: String,
}

impl Answer {
    pub fn new(name: &str, address: &str) -> Answer {
        Answer {
            name: name.to_owned(),
            address: address.to_owned(),
        }
    }

    pub fn get_name_ref(&self) -> &String {
        &self.name
    }

    pub fn get_address_ref(&self) -> &String {
        &self.address
    }
}

impl fmt::Display for Answer {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "[Answer][name={}, address={}]", self.name, self.address)
    }
}
