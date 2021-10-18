package group.yzhs.alarm.config;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.http.converter.HttpMessageConverter;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/10/17 16:46
 * 将枚举类型序列化和反序列化,enum类中会指定@EnumValue注解的field开获取method，然后对其进行调用返回对应的code值进行匹配序列化和反序列化
 * 这里不进行全局配置，只是在dto中指定枚举类型的  @JSONField(deserializeUsing = FastJsonEnumDeserializerAndSerializerConfig.FastJsonEnumDeserializer.class,
 * serializeUsing = FastJsonEnumDeserializerAndSerializerConfig.FastJsonEnumSerializer.class)来指定序列化和反序列化的类
 */
@Configuration
public class FastJsonEnumDeserializerAndSerializerConfig {


    @Bean
    public HttpMessageConverter<?> httpMessageConverter() {
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        fastConverter.getFastJsonConfig().setSerializerFeatures(SerializerFeature.WriteEnumUsingToString);
        return fastConverter;
    }


    @Slf4j
    /**String to enum*/
    public static class FastJsonEnumDeserializer implements ObjectDeserializer
    {

        @Override
        public <T> T deserialze(DefaultJSONParser parser, Type type, Object o) {
            final JSONLexer lexer = parser.lexer;
            Class<?> cls = (Class<?>) type;
            Object[] enumConstants = cls.getEnumConstants();
            Method method = StringToEnumConverterFactory.getMethod(cls);
            if (!Enum.class.isAssignableFrom(cls)) {
                return null;
            }
            for (Object item : enumConstants) {
                String enumcode = null;
                String jsoncode=null;
                try {
                    enumcode= method.invoke(item).toString();//获取指定的枚举类型需要反序列化匹配的值
                    jsoncode=lexer.stringVal();//这里由于传输过来的json，反序列化中对应字段是一个枚举，且json中的字段是一个string
                    Integer integer=lexer.intValue();
                    if (Objects.equals(enumcode, lexer.stringVal()) || Objects.equals(Integer.valueOf(enumcode), lexer.intValue())) {
                        return (T)item;
                    }
                } catch (IllegalAccessException | InvocationTargetException |NumberFormatException ex) {
//                    log.error(String.format("jsonvalue=%s,enumcode=%s",jsoncode,enumcode));
                    //do nothing
                }
            }
            return null;
        }

        @Override
        public int getFastMatchToken() {
            return JSONToken.LITERAL_INT;
        }
    }

    @Slf4j
    /**enum to String*/
    public static class FastJsonEnumSerializer implements ObjectSerializer {
        @Override
        public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {

            /*如果只是将指定注解的数据写入*/
            Method method = StringToEnumConverterFactory.getMethod(object.getClass());
            try {
                Object value=method.invoke(object);
                serializer.write(value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("enum to String error ", e);
                serializer.write(null);
            }
            /*如果是将字段全部写入*/
//            Method[] methods = ReflectUtil.getMethods(object.getClass(), "get");
//            Map<String, Object> objectMap = new HashMap<>(methods.length);
//            for (Method method : methods) {
//                String name = StrUtil.subAfter(method.getName(), "get");
//                // 首字母小写
//                name = name.substring(0, 1).toLowerCase() + name.substring(1);
//                objectMap.put(name, ReflectUtil.invoke(object, method));
//            }
//            serializer.write(objectMap);
        }
    }

 /** 将请求值转换为枚举对象
 * @GetMapping
 *     public String getEnum(StatusEnum status) {
 *         return status.getDesc();
 *     }
 */
    @Slf4j
    public static class StringToEnumConverterFactory implements ConverterFactory<String, Enum<?>> {
        private static final Map<Class<?>, Converter<String, ? extends Enum<?>>> CONVERTER_MAP = new ConcurrentHashMap<>();
        private static final Map<Class<?>, Method> TABLE_METHOD_OF_ENUM_TYPES = new ConcurrentHashMap<>();

        @SneakyThrows
        @Override
        @SuppressWarnings("unchecked cast")
        public <T extends Enum<?>> Converter<String, T> getConverter(Class<T> targetType) {
            // 缓存转换器
            Converter<String, T> converter = (Converter<String, T>) CONVERTER_MAP.get(targetType);
            if (converter == null) {
                converter = new StringToEnumConverter<>(targetType);
                CONVERTER_MAP.put(targetType, converter);
            }
            return converter;
        }

        static class StringToEnumConverter<T extends Enum<?>> implements Converter<String, T> {

            private final Map<String, T> enumMap = new ConcurrentHashMap<>();

            StringToEnumConverter(Class<T> enumType) throws Exception {
                Method method = getMethod(enumType);
                Optional.ofNullable(method).orElseThrow(()->new IllegalArgumentException(String.format("类%s的属性值获取异常",enumType.toString())));
                T[] enums = enumType.getEnumConstants();

                // 将值与枚举对象对应并缓存
                for (T e : enums) {
                    try {
                        enumMap.put(method.invoke(e).toString(), e);
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        log.error("获取枚举值错误!!! ", ex);
                    }
                }
            }


            @Override
            public T convert(@NotNull String source) {
                // 获取
                T t = enumMap.get(source);
                if (t == null) {
                    throw new IllegalArgumentException("该字符串找不到对应的枚举对象 字符串:" + source);
                }
                return t;
            }
        }

        public static <T> Method getMethod(Class<T> enumType) {
            Method method;
            // 找到取值的方法
            if (IEnum.class.isAssignableFrom(enumType)) {
                try {
                    method = enumType.getMethod("getCode");
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException(String.format("类:%s 找不到 getValue方法",
                            enumType.getName()));
                }
            } else {
                method = TABLE_METHOD_OF_ENUM_TYPES.computeIfAbsent(enumType, k -> {
                    Field field =
                            dealEnumType(enumType).orElseThrow(() -> new IllegalArgumentException(String.format(
                                    "类:%s 找不到 @EnumValue注解", enumType.getName())));
                    return getMethod(enumType, field);
                });
            }
            return method;
        }

        private static Optional<Field> dealEnumType(Class<?> clazz) {
            return clazz.isEnum() ?
                    Arrays.stream(clazz.getDeclaredFields()).filter(field -> field.isAnnotationPresent(EnumValue.class)).findFirst() : Optional.empty();
        }

        private static<T> Method getMethod(Class<T> clazz,Field field){
//        PropertyDescriptor pd = null;

            try {
                String getMethodName="get"+field.getName().substring(0,1).toUpperCase()+field.getName().substring(1);
                Method get=clazz.getDeclaredMethod(getMethodName);
//            pd = new PropertyDescriptor(field.getName(), clazz);
//            Method get = pd.getReadMethod();
                get.setAccessible(true);
                return get;
            } catch (NoSuchMethodException e) {
                log.error(String.format("类%s无法找到属性对应的属性%s get方法",clazz.toString(),field.getName()));
            }
            return null;
        }
    }

}
