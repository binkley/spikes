#[cfg(test)]
mod tests {
    #[test]
    fn it_works() {
        assert_eq!(2 + 2, 4);
    }
}

pub mod math {
    pub fn collatz(x: u64) -> u64 {
        if 0 == x % 2 {
            x / 2
        } else {
            x * 3 + 1
        }
    }
}
