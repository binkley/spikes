#[cfg(test)]
mod tests {
    #[test]
    fn collatz_me_base_case() {
        use crate::math;

        let result = math::collatz(1);

        assert_eq!(result, 0);
    }

    #[test]
    fn collatz_me_odd() {
        use crate::math;

        let result = math::collatz(2);

        assert_eq!(result, 1);
    }

    #[test]
    fn collatz_me_even() {
        use crate::math;

        let result = math::collatz(3);

        assert_eq!(result, 7);
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

pub mod layers {
    use std::collections::HashMap;

    #[derive(Debug)]
    pub struct Layer<'a> {
        contents: HashMap<&'a str, i32>,
    }

    impl<'a> Layer<'a> {
        pub fn new() -> Layer<'a> {
            Layer {
                contents: HashMap::new(),
            }
        }

        pub fn insert(&mut self, k: &'a str, v: i32) {
            self.contents.insert(k, v);
        }
    }

    #[derive(Debug)]
    pub struct Layers<'a> {
        layers: Vec<Layer<'a>>,
    }

    impl<'a> Layers<'a> {
        pub fn new() -> Layers<'a> {
            Layers { layers: Vec::new() }
        }

        pub fn new_layer(&mut self) -> &mut Layer<'a> {
            self.layers.push(Layer::new());
            self.layers.last_mut().unwrap()
        }
    }
}
