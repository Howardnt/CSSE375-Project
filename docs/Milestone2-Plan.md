# CSSE 375 — Milestones Plan
**Project:** Java Design Linter
**Team:** Ervin Perkowski + Noah Howard

---

## Milestone 2 (Due: April 2, 2026)

### Features

---

#### Feature 1: SeverityLevel Enum
**Owner:** Ervin

**Files:**
- CREATE `src/main/java/rhit/csse/csse374/linter/domain/SeverityLevel.java`
- MODIFY `src/main/java/rhit/csse/csse374/linter/domain/Violation.java`
- MODIFY `src/main/java/rhit/csse/csse374/linter/presentation/gui/ResultsAccordionPanel.java`
- MODIFY `src/main/java/rhit/csse/csse374/linter/presentation/gui/SeverityCellRenderer.java`

**Tests:**
- `SeverityLevelTest.java` — verify each enum value has a non-null color
- `ViolationTest.java` — verify Violation stores and returns SeverityLevel correctly
- `SeveritySystemTest.java` — run linter on fixture, verify violations carry correct SeverityLevel values

---

#### Feature 2: Export Results to JSON
**Owner:** Howard

**Files:**
- CREATE `src/main/java/rhit/csse/csse374/linter/presentation/ReportRenderer.java` (interface)
- CREATE `src/main/java/rhit/csse/csse374/linter/presentation/JsonRenderer.java`
- CREATE `src/main/java/rhit/csse/csse374/linter/presentation/TextRenderer.java`
- MODIFY `src/main/java/rhit/csse/csse374/linter/presentation/gui/LinterGuiFrame.java` — add Export JSON button

**Tests:**
- `JsonRendererTest.java` — render a LinterResult and verify JSON structure
- `TextRendererTest.java` — verify text output matches existing behavior
- `JsonExportSystemTest.java` — run linter, export JSON, read file back, assert violation count matches

---

### Refactorings

---

#### Refactoring 1: Extract Method — `CohesionAnalyzer.calculateLCOM4()`
**Owner:** Ervin | **Smell:** Long Method

**Files:**
- MODIFY `src/main/java/rhit/csse/csse374/linter/domain/CohesionAnalyzer.java`

**Test:** `CohesionAnalyzerTest.java` — assert calculateLCOM4() returns same value on fixture classes before and after

---

#### Refactoring 2: Extract Method — `ASMMethod` Duplicate Analysis Blocks
**Owner:** Ervin | **Smell:** Duplicate Code

**Files:**
- MODIFY `src/main/java/rhit/csse/csse374/linter/data/ASMMethod.java`

**Test:** `ASMMethodTest.java` — load fixture class, verify both analysis paths return correct frames before and after

---

#### DONE!!!! Refactoring 3: Move Method — `CohesionAnalyzer` Feature Envy
**Owner:** Ervin | **Smell:** Feature Envy

**Files:**
- MODIFY `src/main/java/rhit/csse/csse374/linter/domain/CohesionAnalyzer.java`
- MODIFY `src/main/java/rhit/csse/csse374/linter/data/ASMClass.java` — add isInterface() and isAbstract()

**Test:** `ASMClassTest.java` — load interface and abstract class fixtures, assert isInterface() and isAbstract() return correct values

---

#### Refactoring 4: Extract Method — `LinterGuiFrame.onRun()`
**Owner:** Howard | **Smell:** Long Method

**Files:**
- MODIFY `src/main/java/rhit/csse/csse374/linter/presentation/gui/LinterGuiFrame.java`

**Test:** `LinterGuiFrameTest.java` — verify same checks run and same output produced before and after

---

#### Refactoring 5: Pull Up Method — Hollywood Strategy Duplicate Logic
**Owner:** Howard | **Smell:** Duplicate Code

**Files:**
- MODIFY `src/main/java/rhit/csse/csse374/linter/domain/HollywoodStrategy.java` — add shared default method
- MODIFY `src/main/java/rhit/csse/csse374/linter/domain/StrictHollywoodStrategy.java`
- MODIFY `src/main/java/rhit/csse/csse374/linter/domain/ThresholdHollywoodStrategy.java`
- MODIFY `src/main/java/rhit/csse/csse374/linter/domain/InstantiationOnlyStrategy.java`

**Test:** `HollywoodStrategyTest.java` — assert same violation counts on Hollywood fixtures before and after

---

#### Refactoring 6: Extract Class + Rename — `singleResponsibilityPrinciple`
**Owner:** Howard | **Smell:** Large Class + Naming Violation

**Files:**
- RENAME `singleResponsibilityPrinciple.java` → `SingleResponsibilityPrinciple.java`
- CREATE `src/main/java/rhit/csse/csse374/linter/domain/FieldAccessAnalyzer.java`
- CREATE `src/main/java/rhit/csse/csse374/linter/domain/DependencyFanOutCalculator.java`
- MODIFY `src/main/java/rhit/csse/csse374/linter/domain/SingleResponsibilityPrinciple.java`

**Test:** `SingleResponsibilityPrincipleTest.java` — run check against SingleResponsibilityPrincipleFixture, assert same violation output before and after

---

### Other M2 Tasks
- CREATE `.github/workflows/ci.yml` — GitHub Actions running `mvn test` on push
- CREATE `milestone2` branch in GitHub repo
- Q3: Document two manual exploratory testing sessions in the journal (each person runs linter on an unfamiliar project and notes findings)
- Q4: Write performance and security test plans in the M2 document

---

## Milestone 3

### Features

---

#### Feature 3: CLI Mode
**Owner:** Ervin

**Files:**
- CREATE `src/main/java/rhit/csse/csse374/linter/presentation/LinterCLI.java`
- CREATE `src/main/java/rhit/csse/csse374/linter/presentation/LinterService.java` (extracted from LinterGuiFrame)
- MODIFY `src/main/java/rhit/csse/csse374/linter/presentation/gui/LinterGuiFrame.java`

**Tests:**
- `LinterCLITest.java` — verify CLI args are parsed and correct checks run
- `LinterServiceTest.java` — verify service runs analysis and returns correct results

---

#### Feature 4: Configuration File (JSON)
**Owner:** Howard

**Files:**
- CREATE `src/main/java/rhit/csse/csse374/linter/domain/ConfigLoader.java`
- MODIFY `src/main/java/rhit/csse/csse374/linter/presentation/gui/CheckCatalog.java`

**Tests:**
- `ConfigLoaderTest.java` — verify config file is read and correct checks are enabled/disabled

---

### Refactorings

---

#### Refactoring 7: Extract Class — `LinterGuiFrame` God Class
**Owner:** Ervin | **Smell:** Large Class

**Files:**
- EXTRACT `src/main/java/rhit/csse/csse374/linter/presentation/LinterService.java` from `LinterGuiFrame.java`
- MODIFY `src/main/java/rhit/csse/csse374/linter/presentation/gui/LinterGuiFrame.java`

**Test:** Verify GUI still runs analysis correctly after extraction

---

#### Refactoring 8: Extract Method — `CheckCatalog.toDescriptor()`
**Owner:** Ervin or Howard | **Smell:** Long Method

**Files:**
- MODIFY `src/main/java/rhit/csse/csse374/linter/presentation/gui/CheckCatalog.java`

**Test:** `CheckCatalogTest.java` — verify same checks discovered before and after

---

#### Refactoring 9: Extract Class — `ResultsAccordionPanel` God Class
**Owner:** Howard or Ervin (TBD) | **Smell:** Large Class

**Files:**
- EXTRACT `src/main/java/rhit/csse/csse374/linter/presentation/gui/ViolationFilter.java`
- EXTRACT `src/main/java/rhit/csse/csse374/linter/presentation/gui/CollapsibleCheckPanel.java`
- MODIFY `src/main/java/rhit/csse/csse374/linter/presentation/gui/ResultsAccordionPanel.java`

**Test:** Verify results panel displays same violations after extraction

---

## Milestone 4 — Finalization
- HTML Report feature
- Final test coverage pass
- Installation guide, user documentation, maintenance guide
- Updated UML diagram
