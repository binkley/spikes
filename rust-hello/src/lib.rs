#[cfg(test)]
mod tests {
    #[test]
    fn it_works() {
        assert_eq!(2 + 2, 4);
    }

    #[test]
    fn collatz_me_odd() {
        use crate::math;

        let result = math::collatz(13);

        assert_eq!(result, 40);
    }

    #[test]
    fn collatz_me_even() {
        use crate::math;

        let result = math::collatz(12);

        assert_eq!(result, 6);
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
