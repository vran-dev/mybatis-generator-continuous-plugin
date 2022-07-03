package cc.cc1234.mybatis.generator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;

import java.util.Collections;
import java.util.List;

/**
 * 1. add <b>example()</b> method in Criteria
 * 2. add miss <b>orderBy</b> type
 * 3. add miss <b>selectOneByExample(Example)</b> method
 * 4. add static factory <b>create()</b> method in Example
 */
public class ExampleModelPlusPlugin extends PluginAdapter {

    private static final String DISABLE_ORDER_BY_PROP = "example.order-by.disabled";

    private static final String DISABLE_STATIC_FACTORY_PROP = "example.static-factory.disabled";

    private static final String DISABLE_EXAMPLE_PROP = "criteria.example.disabled";

    private List<String> warnings;

    @Override
    public boolean validate(List<String> warnings) {
        this.warnings = warnings;
        return true;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if ("false".equals(properties.getProperty(DISABLE_EXAMPLE_PROP, "false"))) {
            addExampleMethodInCriteria(topLevelClass);
        }

        if ("false".equals(properties.getProperty(DISABLE_STATIC_FACTORY_PROP, "false"))) {
            addStaticFactoryInExample(topLevelClass);
        }

        if ("false".equals(properties.getProperty(DISABLE_ORDER_BY_PROP, "false"))) {
            addOrderByMethodInExample(topLevelClass);
            addOrderByFieldInExample(topLevelClass);
            updateClearMethodInExample(topLevelClass);
            addOrderByCriteriaClassInExample(topLevelClass, introspectedTable);
            updateGetOrderByClause(topLevelClass);
            updateSetOrderByClause(topLevelClass);
        }
        return true;
    }

    private void addStaticFactoryInExample(TopLevelClass exampleModel) {
        Method method = new Method("create");
        method.setStatic(true);
        method.setReturnType(exampleModel.getType());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addBodyLine("return new " + exampleModel.getType().getShortName() + "();");
        exampleModel.addImportedType(exampleModel.getType());
        exampleModel.addMethod(method);
    }

    private void addExampleMethodInCriteria(TopLevelClass exampleModel) {
        exampleModel.getMethods()
                .stream()
                .filter(method -> method.getName().equals("createCriteriaInternal"))
                .findFirst()
                .ifPresent(method -> {
                    method.getBodyLines().clear();
                    method.addBodyLine("Criteria criteria = new Criteria(this);");
                    method.addBodyLine("return criteria;");
                });

        exampleModel.getInnerClasses()
                .stream()
                .filter(inner -> inner.getSuperClass().isPresent() && inner.isStatic())
                .findFirst()
                .ifPresent(inner -> {
                    FullyQualifiedJavaType exampleType = exampleModel.getType();

                    Field exampleField = new Field("example", exampleType);
                    exampleField.setFinal(true);
                    exampleField.setVisibility(JavaVisibility.PRIVATE);
                    inner.addField(exampleField);

                    inner.getMethods()
                            .stream()
                            .filter(Method::isConstructor)
                            .findFirst()
                            .ifPresent(constructor -> {
                                Parameter exampleParameter = new Parameter(exampleType, "example");
                                constructor.addParameter(exampleParameter);
                                constructor.addBodyLines(Collections.singletonList("this.example = example;"));
                            });

                    Method method = new Method("example");
                    method.setStatic(false);
                    method.setReturnType(exampleModel.getType());
                    method.setVisibility(JavaVisibility.PUBLIC);
                    method.addBodyLine("return this.example;");
                    exampleModel.addImportedType(exampleModel.getType());
                    inner.addMethod(method);
                });
    }

    /**
     * <pre>
     *     public OrderByCriteria orderBy() {
     *         return new OrderByCriteria(this);
     *     }
     * </pre>
     */
    private void addOrderByMethodInExample(TopLevelClass topLevelClass) {
        Method method = new Method("orderBy");
        method.setStatic(false);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType("OrderByCriteria"));
        method.addBodyLine("return new OrderByCriteria(this);");
        topLevelClass.addMethod(method);
    }

    private void updateClearMethodInExample(TopLevelClass topLevelClass) {
        topLevelClass.getMethods()
                .stream()
                .filter(method -> method.getName().equals("clear"))
                .findFirst()
                .ifPresent(method -> {
                    method.addBodyLine("orderBy.clear();");
                });
    }

    private void updateSetOrderByClause(TopLevelClass topLevelClass) {
        topLevelClass.getMethods()
                .stream()
                .filter(method -> method.getName().equals("setOrderByClause"))
                .findFirst()
                .ifPresent(method -> {
                    method.getBodyLines().clear();
                    method.addBodyLine("this.orderBy.add(orderByClause);");
                });
    }

    private void updateGetOrderByClause(TopLevelClass topLevelClass) {
        topLevelClass.getMethods()
                .stream()
                .filter(method -> method.getName().equals("getOrderByClause"))
                .findFirst()
                .ifPresent(method -> {
                    method.getBodyLines().clear();
                    method.addBodyLine("if (orderBy.isEmpty()) {");
                    method.addBodyLine("    return this.orderByClause;");
                    method.addBodyLine("}");
                    method.addBodyLine("else {");
                    method.addBodyLine("    return String.join(\",\", orderBy);");
                    method.addBodyLine("}");
                });
    }

    private void addOrderByFieldInExample(TopLevelClass topLevelClass) {
        FullyQualifiedJavaType type = new FullyQualifiedJavaType("java.util.List");
        type.addTypeArgument(new FullyQualifiedJavaType("String"));
        Field orderByField = new Field("orderBy", type);
        orderByField.setInitializationString("new ArrayList<>()");
        orderByField.setVisibility(JavaVisibility.PRIVATE);
        orderByField.setFinal(true);
        topLevelClass.addField(orderByField);
    }

    private void addOrderByCriteriaClassInExample(TopLevelClass topLevelClass, IntrospectedTable table) {
        TopLevelClass orderByCriteria = new TopLevelClass("OrderByCriteria");
        orderByCriteria.setVisibility(JavaVisibility.PUBLIC);
        orderByCriteria.setStatic(true);

        Field exampleField = new Field("example", topLevelClass.getType());
        exampleField.setVisibility(JavaVisibility.PRIVATE);
        exampleField.setFinal(true);
        orderByCriteria.addField(exampleField);

        Method constructor = new Method("OrderByCriteria");
        constructor.setVisibility(JavaVisibility.PROTECTED);
        constructor.setConstructor(true);
        constructor.addBodyLine("this.example = example;");
        constructor.addParameter(new Parameter(topLevelClass.getType(), "example"));
        orderByCriteria.addMethod(constructor);

        Method exampleMethod = new Method("example");
        exampleMethod.setVisibility(JavaVisibility.PUBLIC);
        exampleMethod.setReturnType(topLevelClass.getType());
        exampleMethod.addBodyLine("return this.example;");
        orderByCriteria.addMethod(exampleMethod);

        table.getAllColumns().forEach(column -> {
            orderByCriteria.addMethod(columnOrderMethod(column, "Asc"));
            orderByCriteria.addMethod(columnOrderMethod(column, "Desc"));
        });

        topLevelClass.addInnerClass(orderByCriteria);
    }

    private Method columnOrderMethod(IntrospectedColumn column, String sort) {
        String columnName = column.getActualColumnName();
        String propName = column.getJavaProperty();
        Method method = new Method(propName + sort);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType("OrderByCriteria"));
        method.addBodyLine("this.example.orderBy.add(\"" + columnName + " " + sort.toUpperCase() + "\");");
        method.addBodyLine("return this;");
        return method;
    }
}
