package cc.cc1234.mybatis.generator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;

import java.util.*;

/**
 * <p>
 * set property <b>ignoreColumns</b> to configure which columns should be ignored, such as: *.created, user.id.
 * </p>
 */
public class NullSafePlugin extends PluginAdapter {

    private static final String IGNORE_COLUMN_PROP = "ignore.columns";

    private static final String ADD_SPRING_NULLABLE_ANNOTATION = "spring.nullable";

    private static final String ADD_OPTIONAL_GETTER = "optional.getter";

    private static final String ADD_CUSTOMIZE_ANNOTATION = "customize.annotation";

    private static final String ALL = "*";

    private Map<String, Set<String>> ignoredTableColumns = new HashMap<>();

    private boolean addSpringNullableAnnotation;

    private boolean addOptionalGetter;

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        String ignoreColumns = (String) super.properties.getOrDefault(IGNORE_COLUMN_PROP, "");
        if (ignoreColumns.isEmpty()) {
            return;
        }

        String[] ignoreColumnsArray = ignoreColumns.split(",");
        for (String ignoreColumnPattern : ignoreColumnsArray) {
            int separatorIndex = ignoreColumnPattern.lastIndexOf(".");
            if (separatorIndex == -1) {
                continue;
            }

            String tableName = ignoreColumnPattern.substring(0, separatorIndex).trim();
            String columnName = ignoreColumnPattern.substring(separatorIndex + 1).trim();
            Set<String> values = ignoredTableColumns.computeIfAbsent(tableName, k -> new HashSet<>());
            values.add(columnName);
        }

        addSpringNullableAnnotation = Boolean.parseBoolean(
                properties.getOrDefault(ADD_SPRING_NULLABLE_ANNOTATION, "false").toString());
        addOptionalGetter = Boolean.parseBoolean(
                properties.getOrDefault(ADD_OPTIONAL_GETTER, "true").toString());
    }

    @Override
    public boolean modelFieldGenerated(Field field,
                                       TopLevelClass topLevelClass,
                                       IntrospectedColumn introspectedColumn,
                                       IntrospectedTable introspectedTable,
                                       ModelClassType modelClassType) {
        if (ignore(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName(),
                introspectedColumn.getActualColumnName())) {
            return true;
        }

        addSpringNullableAnnotation(field, topLevelClass, introspectedColumn);
        addCustomizeAnnotation(field, topLevelClass, introspectedColumn);
        return true;
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method,
                                              TopLevelClass topLevelClass,
                                              IntrospectedColumn introspectedColumn,
                                              IntrospectedTable introspectedTable,
                                              ModelClassType modelClassType) {
        if (ignore(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName(),
                introspectedColumn.getActualColumnName())) {
            return true;
        }

        addOptionalGetter(method, topLevelClass, introspectedColumn);
        addSpringNullableAnnotation(method, topLevelClass, introspectedColumn);
        addCustomizeAnnotation(method, topLevelClass, introspectedColumn);
        return true;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method,
                                              TopLevelClass topLevelClass,
                                              IntrospectedColumn introspectedColumn,
                                              IntrospectedTable introspectedTable,
                                              ModelClassType modelClassType) {
        if (ignore(introspectedTable.getFullyQualifiedTable().getIntrospectedTableName(),
                introspectedColumn.getActualColumnName())) {
            return true;
        }

        Parameter parameter = method.getParameters().iterator().next();
        addSpringNullableAnnotation(parameter, topLevelClass, introspectedColumn);
        addCustomizeAnnotation(parameter, topLevelClass, introspectedColumn);
        return true;
    }

    protected boolean ignore(String tableName, String columnName) {
        if (ignoredTableColumns.containsKey(tableName)) {
            Set<String> ignoredColumns = ignoredTableColumns.get(tableName);
            if (ignoredColumns.contains(columnName)) {
                return true;
            }
        }

        if (ignoredTableColumns.containsKey(ALL)) {
            Set<String> ignoredColumns = ignoredTableColumns.get(ALL);
            if (ignoredColumns.contains(columnName)) {
                return true;
            }
        }
        return false;
    }

    private void addOptionalGetter(Method method,
                                   TopLevelClass topLevelClass,
                                   IntrospectedColumn introspectedColumn) {
        if (addOptionalGetter && introspectedColumn.isNullable()) {
            method.getReturnType().ifPresent(returnType -> {
                topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.Optional"));
                Method optionGetter = new Method(method.getName() + "Optional");
                optionGetter.setVisibility(method.getVisibility());
                String shortName = returnType.getShortName();
                optionGetter.setReturnType(
                        new FullyQualifiedJavaType("java.util.Optional<" + shortName + ">"));
                optionGetter.addBodyLine("return Optional.ofNullable(" + introspectedColumn.getJavaProperty() + ");");
                topLevelClass.addMethod(optionGetter);
            });
        }
    }

    private void addSpringNullableAnnotation(Method method,
                                             TopLevelClass topLevelClass,
                                             IntrospectedColumn introspectedColumn) {
        if (addSpringNullableAnnotation && introspectedColumn.isNullable()) {
            topLevelClass.addImportedType("org.springframework.lang.Nullable");
            method.addAnnotation("@Nullable");
        }
    }

    private void addSpringNullableAnnotation(Field field,
                                             TopLevelClass topLevelClass,
                                             IntrospectedColumn introspectedColumn) {
        if (addSpringNullableAnnotation && introspectedColumn.isNullable()) {
            topLevelClass.addImportedType("org.springframework.lang.Nullable");
            field.addAnnotation("@Nullable");
        }
    }

    private void addSpringNullableAnnotation(Parameter parameter,
                                             TopLevelClass topLevelClass,
                                             IntrospectedColumn introspectedColumn) {
        if (addSpringNullableAnnotation && introspectedColumn.isNullable()) {
            topLevelClass.addImportedType("org.springframework.lang.Nullable");
            parameter.addAnnotation("@Nullable");
        }
    }

    private void addCustomizeAnnotation(Method method,
                                        TopLevelClass topLevelClass,
                                        IntrospectedColumn introspectedColumn) {
        String customizeAnnotation = properties.getProperty(ADD_CUSTOMIZE_ANNOTATION);
        if (customizeAnnotation != null && introspectedColumn.isNullable()) {
            method.addAnnotation(customizeAnnotation);
        }
    }

    private void addCustomizeAnnotation(Field field,
                                        TopLevelClass topLevelClass,
                                        IntrospectedColumn introspectedColumn) {
        String customizeAnnotation = properties.getProperty(ADD_CUSTOMIZE_ANNOTATION);
        if (customizeAnnotation != null && introspectedColumn.isNullable()) {
            field.addAnnotation(customizeAnnotation);
        }
    }

    private void addCustomizeAnnotation(Parameter parameter,
                                        TopLevelClass topLevelClass,
                                        IntrospectedColumn introspectedColumn) {
        String customizeAnnotation = properties.getProperty(ADD_CUSTOMIZE_ANNOTATION);
        if (customizeAnnotation != null && introspectedColumn.isNullable()) {
            parameter.addAnnotation(customizeAnnotation);
        }
    }
}