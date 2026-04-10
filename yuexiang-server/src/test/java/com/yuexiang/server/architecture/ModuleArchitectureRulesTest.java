package com.yuexiang.server.architecture;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleArchitectureRulesTest {

    private static final Path REPO_ROOT = Path.of("").toAbsolutePath().normalize().getParent();
    private static final Set<String> BUSINESS_PACKAGES = Set.of(
            "com.yuexiang.system.",
            "com.yuexiang.user.",
            "com.yuexiang.shop.",
            "com.yuexiang.blog.",
            "com.yuexiang.voucher.",
            "com.yuexiang.ai."
    );

    @Test
    void bizModulesShouldNotDependOnOtherBizModules() throws Exception {
        List<String> violations = new ArrayList<>();

        try (Stream<Path> modules = Files.list(REPO_ROOT)) {
            modules.filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().startsWith("yuexiang-module-"))
                    .forEach(moduleDir -> checkBizPomDependencies(moduleDir, violations));
        }

        assertTrue(
                violations.isEmpty(),
                () -> "Forbidden biz -> biz dependencies found:\n" + String.join("\n", violations)
        );
    }

    @Test
    void frameworkSourceShouldNotImportBusinessModules() throws IOException {
        List<String> violations = new ArrayList<>();
        Path frameworkRoot = REPO_ROOT.resolve("yuexiang-framework");

        try (Stream<Path> files = Files.walk(frameworkRoot)) {
            files.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> path.toString().contains("src\\main\\java"))
                    .forEach(path -> collectFrameworkImportViolations(path, violations));
        }

        assertTrue(
                violations.isEmpty(),
                () -> "Framework modules must not depend on business modules:\n" + String.join("\n", violations)
        );
    }

    @Test
    void codeShouldFollowLayerPackageConventions() throws IOException {
        List<String> violations = new ArrayList<>();

        try (Stream<Path> files = Files.walk(REPO_ROOT)) {
            files.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> path.toString().contains("src\\main\\java") || path.toString().contains("src\\test\\java"))
                    .filter(path -> !path.toString().contains("\\target\\"))
                    .forEach(path -> collectPackageConventionViolations(path, violations));
        }

        assertTrue(
                violations.isEmpty(),
                () -> "Layer package convention violations found:\n" + String.join("\n", violations)
        );
    }

    private void checkBizPomDependencies(Path moduleDir, List<String> violations) {
        String moduleName = moduleDir.getFileName().toString();
        Path bizPom = moduleDir.resolve(moduleName + "-biz").resolve("pom.xml");
        if (!Files.exists(bizPom)) {
            return;
        }

        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bizPom.toFile());
            NodeList dependencies = document.getElementsByTagName("dependency");
            for (int i = 0; i < dependencies.getLength(); i++) {
                Element dependency = (Element) dependencies.item(i);
                NodeList artifactIdNodes = dependency.getElementsByTagName("artifactId");
                if (artifactIdNodes.getLength() == 0) {
                    continue;
                }
                String artifactId = artifactIdNodes.item(0).getTextContent().trim();
                if (artifactId.matches("yuexiang-module-.*-biz")) {
                    violations.add(moduleName + " -> " + artifactId + " (" + bizPom + ")");
                }
            }
        } catch (Exception e) {
            violations.add("Failed to parse " + bizPom + ": " + e.getMessage());
        }
    }

    private void collectFrameworkImportViolations(Path path, List<String> violations) {
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (!trimmed.startsWith("import ")) {
                    continue;
                }
                for (String businessPackage : BUSINESS_PACKAGES) {
                    if (trimmed.contains(businessPackage)) {
                        violations.add(path + " -> " + trimmed);
                    }
                }
            }
        } catch (IOException e) {
            violations.add("Failed to read " + path + ": " + e.getMessage());
        }
    }

    private void collectPackageConventionViolations(Path path, List<String> violations) {
        try {
            String source = Files.readString(path, StandardCharsets.UTF_8);
            String packageLine = source.lines()
                    .map(String::trim)
                    .filter(line -> line.startsWith("package "))
                    .findFirst()
                    .orElse("");

            if (packageLine.contains(".service.Impl;")) {
                violations.add(path + " uses non-standard package '.service.Impl'");
            }
            if (path.getFileName().toString().endsWith("Assembler.java") && !packageLine.contains(".assembler;")) {
                violations.add(path + " should be placed in an assembler package");
            }
            if (path.getFileName().toString().endsWith("Support.java") && !packageLine.contains(".support;")) {
                violations.add(path + " should be placed in a support package");
            }
            if ((path.getFileName().toString().endsWith("Constants.java")
                    || path.getFileName().toString().endsWith("Constant.java"))
                    && !(packageLine.contains(".constant;") || packageLine.contains(".constants;"))) {
                violations.add(path + " should be placed in a constant/constants package");
            }
        } catch (IOException e) {
            violations.add("Failed to read " + path + ": " + e.getMessage());
        }
    }
}
