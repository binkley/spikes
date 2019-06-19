package x;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DependOnMe {
    private final String name;

    String tell() {
        return "Name: " + name;
    }
}
