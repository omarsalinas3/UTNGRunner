# UTNG Runner 🏃‍♂️⌚

Un juego Endless Runner nativo diseñado exclusivamente para **Wear OS**, construido con **Jetpack Compose** y siguiendo estrictamente los principios de **Clean Architecture**.

## 🚀 Características
* **Arquitectura Limpia**: Separación total entre Dominio (lógica pura), Data (datos y sensores) y Presentación (UI).
* **Motor Físico Personalizado**: Lógica de física y colisiones puramente matemáticas, libres de dependencias de Android, corriendo a estables ~60 FPS mediante Canvas.
* **Integración Wearable**: 
  * Control del personaje mediante giros de la **Corona Rotativa** (`RotaryScrollEvent`).
  * Integración con **Health Services** para mostrar la Frecuencia Cardíaca (BPM) en tiempo real en el HUD del jugador.
  * Efectos de **Vibración Háptica** al saltar y recibir daño para mayor inmersión.
* **Persistencia Local**: Guardado automático del récord máximo de puntaje usando `DataStore`.

## 🏗️ Arquitectura (Clean Architecture)

El proyecto está dividido en 3 capas principales dentro del módulo `:wear`:

1. **Domain (`/domain`)**: El corazón del negocio. Modelos inmutables (`GameState`, `Player`), interfaces de repositorios y Casos de Uso. **Código Kotlin 100% puro**, sin dependencias del SDK de Android.
2. **Data (`/data`)**: Implementación de interfaces. Aquí vive `ScoreRepositoryImpl`, `PreferencesDataSource` (DataStore) y `HeartRateDataSource` (Health Services API).
3. **Presentation (`/presentation`)**: Todo lo visual y estado. 
    * `GameEngine`: Función pura encargada de calcular el estado en el frame N+1.
    * `GameViewModel`: Mantiene el `StateFlow` y ejecuta el game loop en corrutinas. Usa *Manual DI* a través de `GameViewModelFactory`.
    * `GameRenderer`: Responsabilidad Única (SRP) de dibujar figuras en el `Canvas`.
    * `GameScreen`: Componentes Compose y manejo de eventos de entrada.

## 🧪 Pruebas Unitarias (Testing)
El `GameEngine` cuenta con pruebas unitarias que verifican:
* La aplicación correcta de la gravedad.
* El cálculo de puntaje y nivel a lo largo del tiempo.
* Las colisiones mediante bounding boxes (AABB) y el descuento de vidas.
Todo ejecutándose directamente en la JVM sin necesidad de emulador de Android.

## 🛠️ Tecnologías Utilizadas
- **Kotlin**
- **Jetpack Compose for Wear OS**
- **StateFlow & Coroutines**
- **Health Services API**
- **DataStore Preferences**
- **JUnit 4**

---

