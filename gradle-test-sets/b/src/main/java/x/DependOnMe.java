package x;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DependOnMe {
    private final String name;

    void show() {
        System.out.println(name);
    }
}
