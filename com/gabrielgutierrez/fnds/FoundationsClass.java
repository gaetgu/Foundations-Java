package com.gabrielgutierrez.fnds;
import java.util.List;
import java.util.Map;

class FoundationsClass extends Instance implements Callable {
    final String name;
    private final Map<String, Function> methods;
    final FoundationsClass superclass;

    FoundationsClass(FoundationsClass metaclass, FoundationsClass superclass, String name,
                     Map<String, Function> methods) {
        super(metaclass);
        this.superclass = superclass;
        this.name = name;
        this.methods = methods;
    }

    Function findMethod(Instance instance, String name) {
        if (methods.containsKey(name)) {
            return methods.get(name).bind(instance);
        }

        if (superclass != null) {
            return superclass.findMethod(instance, name);
        }

        return null;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Instance instance = new Instance(this);
        Function initializer = methods.get("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }

    @Override
    public int arity() {
        Function initializer = methods.get("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }

    @Override
    public String toString() {
        return name;
    }
}