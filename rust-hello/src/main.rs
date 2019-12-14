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

    let very_long: u128 = std::u128::MAX;
    println!("{} vs 0b{:b}", very_long, very_long);
    println!("{:?}", very_long);
    let nada = ();
    // No default formatter -- println!("{}", nada);
    println!("{:?}", nada);

    let way1: u8 = 123;
    let way2 = 123u8;
    println!("{} vs {}", way1, way2);
    // Way 2 helps with coercing expressions to the right type
    println!("{}", 120u8 + 3);

    let b = true;
    println!("This is a smell: {}", b);
    let b = false;
    println!("Why permit redeclaration within the same scope? {}", b);
}
