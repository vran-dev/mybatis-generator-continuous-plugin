package cc.cc1234.mybatis.generator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;

import java.util.List;
import java.util.stream.Collectors;

class LombokPluginTest {

    @Test
    public void testModelGetterMethodGenerated() {
        LombokPlugin lombokPlugin = new LombokPlugin();
        boolean result = lombokPlugin.modelGetterMethodGenerated(null, null, null, null, null);
        Assertions.assertFalse(result);
    }

    @Test
    public void testModelSetterMethodGenerated() {
        LombokPlugin lombokPlugin = new LombokPlugin();
        boolean result = lombokPlugin.modelSetterMethodGenerated(null, null, null, null, null);
        Assertions.assertFalse(result);
    }

    @Test
    public void testModelLombokAnnotations() {
        LombokPlugin lombokPlugin = new LombokPlugin();
        TopLevelClass demo = new TopLevelClass("Demo");
        IntrospectedTable table = new IntrospectedTableMyBatis3Impl();
        boolean result = lombokPlugin.modelBaseRecordClassGenerated(demo, table);
        Assertions.assertTrue(result);

        List<String> annotations = demo.getAnnotations();
        Assertions.assertEquals(4, annotations.size());
        Assertions.assertTrue(annotations.contains("@Data"));
        Assertions.assertTrue(annotations.contains("@Builder"));
        Assertions.assertTrue(annotations.contains("@NoArgsConstructor"));
        Assertions.assertTrue(annotations.contains("@AllArgsConstructor"));

        List<String> importedTypes = demo.getImportedTypes()
                .stream()
                .map(FullyQualifiedJavaType::getFullyQualifiedName)
                .collect(Collectors.toList());
        Assertions.assertTrue(importedTypes.contains("lombok.Data"));
        Assertions.assertTrue(importedTypes.contains("lombok.Builder"));
        Assertions.assertTrue(importedTypes.contains("lombok.NoArgsConstructor"));
        Assertions.assertTrue(importedTypes.contains("lombok.AllArgsConstructor"));
    }
}