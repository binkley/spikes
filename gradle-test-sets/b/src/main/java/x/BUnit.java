package x;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BUnit {
    private final String name;

    String tell() {
        return "Name: " + name;
    }
}
