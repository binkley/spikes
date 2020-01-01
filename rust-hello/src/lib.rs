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

pub mod boxy {
    use std::borrow::Borrow;
    use std::collections::hash_map::{Iter, IterMut};
    use std::collections::HashMap;
    use std::hash::Hash;
    use std::iter::FromIterator;
    use std::ops::Index;

    #[derive(Clone, Debug, Eq)]
    pub struct MapBox<K: Eq + Hash, V> {
        map: HashMap<K, V>,
    }

    impl<K: Hash + Eq, V> MapBox<K, V> {
        pub fn new() -> MapBox<K, V> {
            MapBox {
                map: HashMap::new(),
            }
        }

        #[inline]
        pub fn iter(&self) -> Iter<'_, K, V> {
            self.map.iter()
        }

        #[inline]
        pub fn iter_mut(&mut self) -> IterMut<'_, K, V> {
            self.map.iter_mut()
        }

        pub fn len(&self) -> usize {
            self.map.len()
        }

        pub fn is_empty(&self) -> bool {
            self.map.is_empty()
        }

        pub fn insert(&mut self, k: K, v: V) -> Option<V> {
            self.map.insert(k, v)
        }
    }

    impl<K, V> PartialEq for MapBox<K, V>
    where
        K: Eq + Hash,
        V: PartialEq,
    {
        fn eq(&self, other: &MapBox<K, V>) -> bool {
            self.map == other.map
        }
    }

    impl<K, V> Default for MapBox<K, V>
    where
        K: Eq + Hash,
    {
        #[inline]
        fn default() -> Self {
            MapBox::new()
        }
    }

    impl<K, Q: ?Sized, V> Index<&Q> for MapBox<K, V>
    where
        K: Eq + Hash + Borrow<Q>,
        Q: Eq + Hash,
    {
        type Output = V;

        #[inline]
        fn index(&self, key: &Q) -> &V {
            self.map.index(key)
        }
    }

    impl<'a, K: Hash + Eq, V> IntoIterator for &'a MapBox<K, V> {
        type Item = (&'a K, &'a V);
        type IntoIter = Iter<'a, K, V>;

        #[inline]
        fn into_iter(self) -> Self::IntoIter {
            self.iter()
        }
    }

    impl<'a, K: Hash + Eq, V> IntoIterator for &'a mut MapBox<K, V> {
        type Item = (&'a K, &'a mut V);
        type IntoIter = IterMut<'a, K, V>;

        #[inline]
        fn into_iter(self) -> Self::IntoIter {
            self.iter_mut()
        }
    }

    impl<K, V> FromIterator<(K, V)> for MapBox<K, V>
    where
        K: Eq + Hash,
    {
        fn from_iter<T: IntoIterator<Item = (K, V)>>(iter: T) -> MapBox<K, V> {
            let mut map = MapBox::new();
            map.extend(iter);
            map
        }
    }

    impl<K, V> Extend<(K, V)> for MapBox<K, V>
    where
        K: Eq + Hash,
    {
        #[inline]
        fn extend<T: IntoIterator<Item = (K, V)>>(&mut self, iter: T) {
            self.map.extend(iter)
        }
    }

    /* TODO
    impl<'a, K, V> Extend<(&'a K, &'a V)> for MapBox<K, V>
    where
        K: Eq + Hash + Copy,
        V: Copy,
    {
        #[inline]
        fn extend<T: IntoIterator<Item = (&'a K, &'a V)>>(&mut self, iter: T) {
            self.map.extend(iter)
        }
    }
    */
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
                c /= 2
            } else {
                c = c * 3 + 1
            }
        }
    }
}

pub mod layers {
    use std::collections::HashMap;
    use std::fmt::{Display, Error, Formatter};
    use std::ops::{Index, IndexMut};

    #[derive(Debug)]
    pub enum Value {
        // TODO: struct and extension rather than discriminated union?
        Text(String),
        Number(i32),
        Amount(f32),
    }

    impl Value {
        // TODO: How to name all these just `new`?

        pub fn text(text: &str) -> Value {
            Value::Text(String::from(text))
        }

        pub fn number(number: i32) -> Value {
            Value::Number(number)
        }

        pub fn amount(amount: f32) -> Value {
            Value::Amount(amount)
        }
    }

    impl Display for Value {
        fn fmt(&self, f: &mut Formatter<'_>) -> Result<(), Error> {
            match self {
                Self::Text(text) => text.fmt(f),
                Self::Number(number) => number.fmt(f),
                Self::Amount(amount) => amount.fmt(f),
            }
        }
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
            let mut layers = Layers { layers: Vec::new() };
            layers.new_layer();
            layers
        }

        pub fn current(&mut self) -> &mut Layer<'a> {
            self.layers.last_mut().unwrap()
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
