use std::fmt;
use std::io::{stdout, BufWriter};

use ferris_says::say;

#[derive(Debug)]
struct Structure(i32);

impl fmt::Display for Structure {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.0)
    }
}

#[derive(Debug)]
struct Deep(Structure);

fn main() {
    let stdout = stdout();
    let out = b"Hello fellow Rustaceans!";
    let width = 24;

    let mut writer = BufWriter::new(stdout.lock());
    say(out, width, &mut writer).unwrap();

    println!("Now {} will print!", Structure(2));
    println!("Now {:?} will print!", Structure(3));
    println!("Now {:?} will print!", Deep(Structure(7)));
}
