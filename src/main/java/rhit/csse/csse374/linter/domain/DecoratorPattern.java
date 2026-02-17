package rhit.csse.csse374.linter.domain;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import rhit.csse.csse374.linter.data.ASMClass;

import java.util.List;

// Noah Howard
public class DecoratorPattern extends Pattern {

    @Override
    public String name() {
        return "Decorator";
    }

    @Override
    protected boolean isPattern(ASMClass cls) {
        ClassNode node = cls.getClassNode();
        
        String superName = node.superName;
        List<String> interfaces = node.interfaces;

        for (FieldNode field : node.fields) {
            String fieldType = getCleanType(field.desc);

            boolean matchesSuper = fieldType.equals(superName);
            boolean matchesInterface = interfaces != null && interfaces.contains(fieldType);

            if (matchesSuper || matchesInterface) {
                if (!fieldType.equals("java/lang/Object")) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getCleanType(String desc) {
        if (desc.startsWith("L") && desc.endsWith(";")) {
            return desc.substring(1, desc.length() - 1);
        }
        return desc;
    }
}

