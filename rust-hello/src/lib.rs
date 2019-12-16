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

        assert_eq!(result, 9);
    }

    #[test]
    fn collatz_me_even() {
        use crate::math;

        let result = math::collatz(12);

        assert_eq!(result, 9);
    }
}

pub mod math {
    // TODO: generic `Unsigned`, u128
    pub fn collatz(x: u64) -> u32 {
        let mut c = x;
        let mut n = 0;
        loop {
            if 1 == c {
                break n;
            }
            n += 1;
            if 0 == c % 2 {
                c = c / 2
            } else {
                c = c * 3 + 1
            }
        }
    }
}
