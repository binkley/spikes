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
    use std::ops::{Index, IndexMut};

    #[derive(Debug)]
    pub enum Value {
        Text(String),
        Number(i32),
        Amount(f32),
    }

    #[derive(Debug)]
    pub struct Layer<'a> {
        contents: HashMap<&'a str, Value>,
    }

    impl<'a> Layer<'a> {
        pub fn new() -> Layer<'a> {
            Layer {
                contents: HashMap::new(),
            }
        }

        pub fn insert(&mut self, k: &'a str, v: Value) {
            self.contents.insert(k, v);
        }
    }

    impl<'a> Index<&'a str> for Layer<'a> {
        type Output = Value;

        #[inline]
        fn index(&self, key: &'a str) -> &Value {
            self.contents.get(key).expect("no entry found for key")
        }
    }

    impl<'a> IndexMut<&'a str> for Layer<'a> {
        #[inline]
        fn index_mut(&mut self, key: &'a str) -> &mut Value {
            if !self.contents.contains_key(key) {
                self.contents.insert(key, Value::Number(0));
            }
            self.contents.get_mut(key).expect("no entry found for key")
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

        pub fn to_hashmap(&self) -> HashMap<&str, &Value> {
            let mut map = HashMap::new();
            for layer in &self.layers {
                for (k, v) in &layer.contents {
                    map.insert(*k, v);
                }
            }
            map
        }
    }

    impl<'a> IntoIterator for &'a Layers<'a> {
        type Item = &'a Layer<'a>;
        type IntoIter = std::slice::Iter<'a, Layer<'a>>;

        fn into_iter(self) -> Self::IntoIter {
            self.layers.iter()
        }
    }
}
