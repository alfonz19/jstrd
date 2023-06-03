import ognl.Ognl;
import ognl.OgnlException;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

public class TestOgnl {

    @Test
    void getStringByKey() throws OgnlException {
        String entryKey = "key";
        String entryValue = "testValue";
        Object value = Ognl.getValue(Ognl.parseExpression(entryKey), Map.of(entryKey, entryValue));

        assertThat(value, is(entryValue));
        assertThat(value, instanceOf(String.class));
    }

    @Test
    void GetNotExistingKey() throws OgnlException {
        String entryKey = "key";
        Object value = Ognl.getValue(Ognl.parseExpression(entryKey), Map.of());

        assertThat(value, nullValue());
    }


    @Test
    void getBooleanByKey() throws OgnlException {
        String entryKey = "key";
        boolean entryValue = true;
        Object value = Ognl.getValue(Ognl.parseExpression(entryKey), Map.of(entryKey, entryValue));

        assertThat(value, is(true));
        assertThat(value, instanceOf(Boolean.class));
    }

    @Test
    void stringComparisonReturningTrue() throws OgnlException {
        String entryKey = "key";
        String entryValue = "testValue";

        String expression = String.format("%s == \"%s\"", entryKey, entryValue);
        Object value = Ognl.getValue(Ognl.parseExpression(expression), Map.of(entryKey, entryValue));

        assertThat(value, is(true));
        assertThat(value, instanceOf(Boolean.class));
    }

    @Test
    void stringComparisonReturningFalse() throws OgnlException {
        String entryKey = "key";
        String entryValue = "testValue";

        String expression = String.format("%s == \"something else\"", entryKey);
        Object value = Ognl.getValue(Ognl.parseExpression(expression), Map.of(entryKey, entryValue));

        assertThat(value, is(Boolean.FALSE));
        assertThat(value, instanceOf(Boolean.class));
    }


    @Test
    void booleanComparisonReturningTrue() throws OgnlException {
        String entryKey = "key";
        boolean entryValue = true;

        String expression = String.format("%s == true", entryKey);
        Object value = Ognl.getValue(Ognl.parseExpression(expression), Map.of(entryKey, entryValue));

        assertThat(value, is(true));
        assertThat(value, instanceOf(Boolean.class));
    }

    @Test
    void booleanComparisonReturningFalse() throws OgnlException {
        String entryKey = "key";
        boolean entryValue = false;

        String expression = String.format("%s == true", entryKey);
        Object value = Ognl.getValue(Ognl.parseExpression(expression), Map.of(entryKey, entryValue));

        assertThat(value, is(Boolean.FALSE));
        assertThat(value, instanceOf(Boolean.class));
    }

    @Test
    void booleanComparisonOverWrongType() {
        String entryKey = "key";
        String entryValue = "something else";


        Assertions.assertThrows(NumberFormatException.class, ()->{
            String expression = String.format("%s == true", entryKey);
            Ognl.getValue(Ognl.parseExpression(expression), Map.of(entryKey, entryValue));
        });
    }

    @Test
    void methodCallAndBooleanComparisonReturningTrue() throws OgnlException {
        Object value = Ognl.getValue(Ognl.parseExpression("size() == 3"), Map.of(1,2,3,4,5,6));

        assertThat(value, is(true));
        assertThat(value, instanceOf(Boolean.class));
    }

//    public static void mmain(String[] args) throws OgnlException {
//
//        Map<String, Object> root = new HashMap<>();
//        root.put("null", null);
//
//
//        test(root, "bool == true", "boolean evaluation");
//
//
//    }
//
//    private static void test(Map<String, Object> map, String expression, String description) {
//        try {
//            Object value = Ognl.getValue(Ognl.parseExpression(expression), map);
//            System.out.printf("%s: %s%n", description, toValueWithItsTypeString(value));
//        } catch (Exception e) {
//            System.out.printf("%s: %s%n", description, "error");
//        }
//    }
//
//    private static String toValueWithItsTypeString(Object value) {
//        return String.format("Value \"%s\" having type %s", value, value==null?"null":value.getClass());
//    }
//
//    public static class  A {
//       private final String value;
//
//        public A(String value) {
//            this.value = value;
//        }
//
//        public String getValue() {
//            return value;
//        }
//    }
}
