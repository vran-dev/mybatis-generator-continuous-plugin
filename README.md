# Mybatis generator Plugin

## NullSafePlugin

### Usage
```xml
<plugin type="cc.cc1234.mybatis.plugin.NullSafePlugin">
    <!-- 会忽略对指定列的处理 -->
    <property name="ignore.columns"
              value="*.create_time,*.update_time,*.create_at,*.active_from,*.active_to,*.operation_time,point_rule.start_time,point_rule.end_time,*.event_properties,*.reward"/>
    <!-- 是否生成返回值为 Optional 的 get 方法 -->
    <property name="optional.getter" value="true"/>
    <!-- 是否添加 spring 的 @Nullable 注解 -->
    <property name="spring.nullable" value="false"/>
    <!-- 添加自定义注解 -->
    <property name="customize.annotation" value="cc.cc1234.Nullable"/>
</plugin>
```

### Show

```java

import java.util.Optional;

public class User {

    private Long id;

    private String email;

    @cc.cc1234.Nullable
    private String street;

    public Optional<String> getStreetOptional() {
        return Optional.ofNullable(this.street);
    }
    
    /* ... */
}

```

## LombokPlugin

###  Usage
```xml

<plugin type="cc.cc1234.mybatis.generator.LombokPlugin">
</plugin>
```

### Show

```java

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    private Long id;
    
    private String email;
    
    private String street;
}

```

## MapperPlusPlugin

### Usage
```xml
<plugin type="cc.cc1234.mybatis.plugin.MapperPlusPlugin">
    <!-- BaseMapper 配置目录-->
    <property name="base-mapper.target.project" value="${mybatis.generator.javaProjectDir}"/>
    <property name="base-mapper.target.package" value="${mybatis.generator.mapper.package}"/>
    <!-- BaseMapper 的名称 -->
    <property name="base-mapper.name" value="OneMapper"/>
    <!-- 每个 Table 对应的 mapper 配置目录 -->
    <property name="java-mapper.target.project" value="src/main/java"/>
    <property name="java-mapper.target.package" value="${mybatis.generator.mapper.package}"/>
    <!-- 是否禁用 selectOneByExample，默认为 false -->
    <property name="select-one-by-example.disabled" value="false"/>
</plugin>
```

### Show
- BaseMapper

```java
public interface OneMapper<T, E>  {
    long countByExample(E example);

    int deleteByExample(E example);

    int deleteByPrimaryKey(Long id);

    int insert(T row);

    int insertSelective(T row);

    Optional<T> selectOneByExample(E example);

    T selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") T row, @Param("example") E example);

    int updateByExample(@Param("row") T row, @Param("example") E example);

    int updateByPrimaryKeySelective(T row);
}
```

- selectOneByExample

```java
public interface OneMapper<T, E>  {
		/* ... */
    List<T> selectByExample(E example);
		/* ... */
}
```

- Java mapper

```java
import cc.cc1234.dao.model.Address;
import cc.cc1234.dao.model.AddressExample;

public interface AddressMapper extends OneMapper<Address, AddressExample> {

}
```

## ExampleModelPlusPlugin

### Usage

```xml
<plugin type="cc.cc1234.mybatis.generator.ExampleModelPlusPlugin">
    <!-- 禁用 order by 增强，默认 false -->
    <property name="example.order-by.disabled" value="false"/>
    <!-- 禁用生成 Example 的静态工厂方法，默认 false -->
    <property name="example.static-factory.disabled" value="false"/>
    <!-- 禁用生成 Criteria 的 example() 方法生成，默认 false -->
    <property name="criteria.example.disabled" value="false"/>
</plugin>
```
### Show
```java
// 使用示例
public class UserService {
    
    public User selectByUsername(String usernameLike) {
        UserExample example = UserExample.create() // 静态工厂方法
                .createCriteria()
                .andUsernameLike(usernameLike)
                .andCreateAtLessThan(LocalDateTime.now())
                .example() // 获取当前 Example 的实例
                .orderBy() // 获取一个 OrderByCriteria 类
                .idDesc() // 该类为所有字段都生成了排序方法
                .createAtDesc()
                .example(); // 获取当前 Example 的实例
// select * from user where username like 'root%' and create_at <= now() order by id desc, create_at desc
        return userMapper.selectByExample(example);
    }
}
```
