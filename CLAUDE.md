# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Scene is an Android lightweight navigation and UI composition framework based on views, designed to replace Activities and Fragments. It provides better performance (100ms+ faster than Activity startup), simpler navigation stack management, and full Jetpack Architecture Components support while maintaining Fragment compatibility.

Key characteristics:
- Single-Activity architecture with view-based navigation
- Stack-based navigation model (not navigation graph based)
- Hierarchical scene composition via GroupScene
- Full support for Lifecycle, ViewModel, SavedStateRegistry
- Modular architecture with 8 separate library modules

## Build Commands

### Building the project
```bash
./gradlew build
```

### Running all tests
```bash
./gradlew test
```

### Running tests for a specific module
```bash
./gradlew :library:scene:test
./gradlew :library:scene_navigation:test
```

### Running a single test class
```bash
./gradlew :library:scene_navigation:test --tests NavigationSceneLifecycleTests
./gradlew :library:scene:test --tests SceneLifecycleTests
```

### Installing the demo app
```bash
./gradlew installDebug
```

### Building specific modules
```bash
./gradlew :library:scene:assemble
./gradlew :library:scene_navigation:assemble
```

### Clean build
```bash
./gradlew clean build
```

## Project Structure

### Module Architecture

The project is divided into 8 library modules with clear dependencies:

1. **scene** (Base module)
   - Core Scene, GroupScene, State classes
   - Lifecycle management (6-state machine: NONE → CREATED → VIEW_CREATED → ACTIVITY_CREATED → STARTED → RESUMED)
   - Scope system for hierarchical resource management
   - No navigation features - purely scene composition and lifecycle

2. **scene_navigation** (Navigation engine)
   - NavigationScene: Stack-based navigation controller
   - NavigationSceneManager: Core navigation orchestration (1922 lines)
   - NavigationMessageQueue: Sequential operation execution
   - Push/pop operations with animation coordination
   - Scene reuse pool system for memory efficiency

3. **scene_ui** (UI templates)
   - SceneActivity: Activity wrapper to host scenes
   - AppCompatScene: Material Design support
   - SwipeBackAppCompatScene: Gesture-driven back navigation

4. **scene_navigation_compose** (Jetpack Compose integration)
   - ComposeScreen: Embed Compose UI in Scene framework
   - Lifecycle-aware recomposition

5. **scene_fragment** (Fragment bridge)
   - FragmentScene: Use Scenes within Fragment context
   - Fragment lifecycle mapping to Scene lifecycle

6. **scene_dialog** (Dialog utilities)
   - Transparent Scene-based dialog implementation

7. **scene_shared_element_animation** (Shared element transitions)
   - Scene-to-scene transition animations

8. **scene_ktx** (Kotlin extensions)
   - Extension functions for idiomatic Kotlin usage

### Key Architecture Patterns

#### Scene Lifecycle State Machine

Scenes follow a 6-state lifecycle:
```
NONE (0) → CREATED (1) → VIEW_CREATED (2) → ACTIVITY_CREATED (3) → STARTED (4) → RESUMED (5)
```

Parent-to-child synchronization ensures children reach lifecycle states after parents during entry, and before parents during exit.

#### Navigation Operation Pattern

Navigation operations (push/pop) are broken into atomic operations executed sequentially:

**Push sequence:**
1. PushCreateOperation - Create new scene
2. PushAnimationOperation - Animate in
3. PushPauseOperation - Pause previous scene
4. PushStopOperation - Stop previous scene

**Pop sequence:**
1. PopAnimationOperationV2 - Animate out
2. PopDestroyMiddlePageOperationV2 - Clean up middle pages
3. PopResumeOperation - Resume previous scene
4. PopDestroyOperation - Destroy current scene

All operations go through NavigationMessageQueue to ensure sequential, non-overlapping execution.

#### Scope System

Parallel to the scene hierarchy is a Scope hierarchy that manages:
- Scene-scoped services and dependencies
- ViewModel stores
- SavedState management
- Automatic cleanup on scope destruction

File: `library/scene/src/main/java/com/bytedance/scene/Scope.java`

#### Scene Reuse System

Optional memory optimization that allows Scene instances to be recycled:
- `IReusePool`: Interface for custom reuse pool implementations
- `DefaultReusePool`: Built-in implementation
- `ReuseBehavior`: Defines matching strategy for reusable scenes
- `NavigationReuseManager`: Orchestrates reuse lifecycle

Location: `library/scene_navigation/src/main/java/com/bytedance/scene/navigation/reuse/`

## Core Concepts

### Scene vs Fragment vs Activity

- **Scene** is analogous to Fragment but lighter and more performant
- Scenes live within a single Activity (typically SceneActivity)
- NavigationScene manages a stack of scenes similar to FragmentManager's back stack
- Key method mapping:
  - Fragment's `onCreateView()` → Scene's `onCreateView()`
  - Fragment's `onViewCreated()` → Scene's `onViewCreated()`
  - FragmentManager's `beginTransaction().replace()` → NavigationScene's `push()`

### GroupScene vs Scene

- **Scene**: Leaf component with a single view
- **GroupScene**: Container that can host multiple child scenes
- GroupScene manages child lifecycle synchronization
- Use GroupScene when you need composition (like ViewPager, tabs, or nested navigation)

### NavigationScene

The main navigation controller - there should be exactly one per Activity:
- Cannot be subclassed (sealed implementation)
- Manages back stack of scenes
- Coordinates animations between scenes
- Handles system back press
- Key API: `push()`, `pop()`, `replace()`, `popTo()`

File: `library/scene_navigation/src/main/java/com/bytedance/scene/navigation/NavigationScene.java` (87KB+)

### NavigationSceneManager

The internal engine that powers NavigationScene:
- Maintains RecordStack (the scene back stack)
- Executes all navigation operations
- Manages scene state transitions
- Coordinates with animation system
- Handles operation queueing via NavigationMessageQueue

File: `library/scene_navigation/src/main/java/com/bytedance/scene/navigation/NavigationSceneManager.java`

## Testing Infrastructure

### Test Framework

The project uses Robolectric for unit testing Android components:
- Robolectric 4.12.2 for Android component testing
- JUnit 4.13.2 as test runner
- Truth 1.1.3 for assertions
- Tests run with `includeAndroidResources = true` in Gradle config

### Test Conventions

Test files are located in `src/test/java/` directories:
- Lifecycle tests: `NavigationSceneLifecycleTests.java`
- Animation tests: `NavigationAnimationExecutorTests.kt`
- Component tests: `ChildSceneLifecycleCallbacksTests.java`

Tests execute with detailed logging showing pass/fail counts (configured in build.gradle).

### Running Tests

Tests can be run at module level or individual class level. The build.gradle configures test output to show failures immediately and summary statistics after completion.

## Important Implementation Notes

### Thread Safety

- Scene creation was historically restricted to UI thread but this has been relaxed
- NavigationScene operations must still be called on UI thread
- NavigationMessageQueue ensures sequential execution of navigation operations to prevent race conditions

### Animation System

The animation system is pluggable via `NavigationAnimationExecutor`:
- Default implementation: `Android8DefaultSceneAnimatorExecutor`
- Custom animations: Implement `NavigationAnimationExecutor`
- Animation resources: Use `AnimationOrAnimatorResourceExecutor`
- Gesture animations: `InteractionNavigationPopAnimationFactory`

Location: `library/scene_navigation/src/main/java/com/bytedance/scene/animation/`

### State Preservation

Scenes support state preservation via:
- `onSaveInstanceState(Bundle)` / `onViewStateRestored(Bundle)` callbacks
- SavedStateRegistry integration (Jetpack standard)
- Can be disabled per-scene with `supportRestore()` override
- Activity-level control via `SceneActivity.supportRestore()`

### Memory Management

The reuse pool system reduces GC pressure by recycling Scene instances. To use:
1. Make Scene implement `IReuseScene`
2. Define `ReuseBehavior` for matching
3. Configure `NavigationReuseManager`
4. Scenes are automatically returned to pool on pop

## Common Patterns

### Creating a Basic Scene

```kotlin
class MyScene : Scene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        return TextView(requireSceneContext()).apply {
            text = "Hello Scene"
        }
    }
}
```

### Navigation Between Scenes

```kotlin
// Push a new scene
navigationScene?.push(DetailScene())

// Pop current scene
navigationScene?.pop()

// Replace current scene
navigationScene?.replace(NewScene())

// Pop to specific scene
navigationScene?.popTo(homeScene)
```

### Using GroupScene for Composition

```kotlin
class ContainerScene : GroupScene() {
    override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return FrameLayout(requireSceneContext()).apply {
            id = View.generateViewId()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        add(viewId, ChildScene(), "child_tag")
    }
}
```

### Setting up SceneActivity

```kotlin
class MainActivity : SceneActivity() {
    override fun getHomeSceneClass(): Class<out Scene> {
        return MainScene::class.java
    }

    override fun supportRestore(): Boolean {
        return false  // or true to enable state restoration
    }
}
```

## Key Files Reference

When working with specific functionality, these are the most important files:

| Component | File Path | Purpose |
|-----------|-----------|---------|
| Scene Base | `library/scene/src/main/java/com/bytedance/scene/Scene.java` | Foundation class (1588 lines) |
| GroupScene | `library/scene/src/main/java/com/bytedance/scene/group/GroupScene.java` | Container for child scenes |
| State Machine | `library/scene/src/main/java/com/bytedance/scene/State.java` | Lifecycle states enum |
| NavigationScene | `library/scene_navigation/src/main/java/com/bytedance/scene/navigation/NavigationScene.java` | Navigation controller (87KB) |
| NavigationSceneManager | `library/scene_navigation/src/main/java/com/bytedance/scene/navigation/NavigationSceneManager.java` | Navigation engine (1922 lines) |
| NavigationMessageQueue | `library/scene_navigation/src/main/java/com/bytedance/scene/queue/NavigationMessageQueue.java` | Operation sequencing |
| Scope | `library/scene/src/main/java/com/bytedance/scene/Scope.java` | Resource scoping |
| SceneActivity | `library/scene_ui/src/main/java/com/bytedance/scene/ui/SceneActivity.java` | Activity integration |

## Development Environment

- **Language**: Java and Kotlin (Kotlin 1.7.21)
- **Build System**: Gradle 8.2.2
- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34
- **Key Dependencies**: AndroidX (Lifecycle, ViewModel, SavedState), Material Components

## Documentation

- Wiki: https://github.com/bytedance/scene/wiki
- Sample APK: https://github.com/bytedance/scene/blob/master/misc/latest_sample.apk
- Compose Integration: https://github.com/bytedance/scene/wiki/Compose
