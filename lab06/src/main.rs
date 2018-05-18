/*
 * Heim László
 * hlim1626
 * 522-es csoport
 * Lab06, DNS
 */
extern crate simpledns;

use simpledns::dns::dns_lookup;
use std::net::{IpAddr, SocketAddr};
use std::env;

fn main() {
    let args: Vec<String> = env::args().collect();
    if args.len() < 3 {
        eprintln!("Usage: {} <nameserver-address> <name-to-find>", args[0]);
        return;
    }

    let dns_addr = SocketAddr::new(IpAddr::V4(args[1].parse().unwrap()), 53);

    let answers = dns_lookup("0.0.0.0:8990", &dns_addr, &args[2])
        .expect("Failed to query dns data");

    for answer in answers {
        println!("Name:    {}", answer.get_name_ref());
        println!("Address: {}", answer.get_address_ref());
    }
}
