#![deny(warnings)]

use std::fmt;
use std::fmt::Display;
use std::io::{stdout, BufWriter};

use ferris_says::say;

use hello_rust::math::collatz;

#[derive(Debug)]
struct Structure(i32);

impl fmt::Display for Structure {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.0)
    }
}

#[derive(Debug)]
struct Deep(Structure);

fn longest_with_an_announcement<'a, T>(x: &'a str, y: &'a str, ann: T) -> &'a str
where
    T: Display,
{
    println!("Announcement! {}", ann);
    if x.len() > y.len() {
        x
    } else {
        y
    }
}

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
    let _b = true;
    println!("Yes, the whole \"don't worry\" ignore warnings needed to port software :)");

    let (a, b, c) = ('a', "b", 0xC);
    println!("Destructured: {}, {}, and {}.", a, b, c);

    let string1 = String::from("abcd");
    let string2 = "xyz";

    let result = longest_with_an_announcement(string1.as_str(), string2, "Hey Dol, Merry dol!");
    println!("The longest string is {}", result);

    println!("{}", collatz(123))
}
