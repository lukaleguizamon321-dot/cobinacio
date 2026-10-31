# Cobinatio v1.4.0 (Forge 1.20.1)

Combina hechizos de Iron's Spells 'n Spellbooks con mobs de otros mods.

- **Revive Mutantes (X/Y/Z)**: combina **Revivir Muertos** (Iron's Spells 'n Spellbooks 3.16.3) con las químicas de **Mutant Monsters** y **Mutant More**.
Agrega un hechizo nuevo en tres versiones: **Revive Mutantes (X)**, **(Y)** y **(Z)**.

## Cómo funciona en el juego

1. Conseguí un **pergamino de Revivir Muertos al nivel máximo (6)**.
2. Ponelo en el **yunque arcano**: pergamino a la izquierda, la química a la derecha.
3. Sacá el resultado: un pergamino de **Revive Mutantes (X)**, **(Y)** o **(Z)** (el nivel del hechizo se mantiene).
4. Pasalo a tu libro en la **mesa de inscripción**, como cualquier otro hechizo. Como es un hechizo de verdad, queda guardado en el libro: si sacás el libro y lo volvés a poner, no se pierde.

| Química | Hechizo | Probabilidad por cada zombi / esqueleto |
|---|---|---|
| Química X (Mutant Monsters) | Revive Mutantes (X) | 25 % |
| Fórmula Y (Mutant More) | Revive Mutantes (Y) | 50 % |
| Compuesto Z (Mutant More) | Revive Mutantes (Z) | 100 % |

También hay una pestaña nueva en el **modo creativo** llamada **Revive Mutantes** con los tres pergaminos.

## Reciclar el pergamino

Si hacés clic derecho con un pergamino de Revive Mutantes sobre un **caldero alquímico** con al menos 250 de agua, en vez de tinta mágica te devuelve **Química X** (siempre la X, nunca la Y ni la Z). Usa la misma probabilidad de reciclado que Iron's (50 % por defecto; si falla, se pierde el pergamino).

## Detalles

- Los mutantes te protegen: no te atacan a vos ni a otros jugadores, aldeanos, mascotas ni invocados.
- Duran 10 minutos (o hasta que vuelvas a lanzar el hechizo para despedirlos), te siguen, y atacan a los monstruos hostiles que se te acercan.
- No sueltan botín ni experiencia.
- Se puede cambiar todo (probabilidades, nivel necesario, qué mutantes salen) en `config/mutantraisedead-common.toml` (se crea la primera vez que abrís el juego con el mod).

## Si venís de una versión anterior

Borrá el `.jar` viejo de la carpeta `mods` (ahora se llama `Cobinatio-1.4.0.jar`). Los libros marcados con la versión 1.0.0 ya no funcionan: hay que hacer el pergamino de nuevo con el yunque.

## Enraizar Plantas (1 a 4) — necesita Mowzie's Mobs

1. Conseguí un **pergamino de Enraizar al nivel máximo (10)**.
2. En el **yunque arcano** ponelo a la izquierda y una **Semilla de Foliaath** (Mowzie's Mobs) a la derecha. Sale **Enraizar Plantas (1)**.
3. Cada semilla más (pergamino de Enraizar Plantas + semilla) lo sube un nivel, hasta **(4)**.
4. El nivel es la cantidad de plantas: al lanzarlo, el objetivo queda inmovilizado como con Enraizar y aparecen 1 a 4 plantas Foliaath en forma de X, a un bloque de distancia, que lo muerden hasta que termina el hechizo (después desaparecen).

Las plantas solo atacan al objetivo inmovilizado (mob o jugador), nunca a vos. No sueltan botín. El daño de cada mordisco se cambia en `config/mutantraisedead-common.toml` (`plantDamage`).

Ambos hechizos están en la pestaña **Cobinatio** del modo creativo.

## Cangrejos de materiales (6 variantes)

Variantes del cangrejo de diamante (Grottol) de Mowzie's Mobs. Son mobs propios de Cobinatio (no hace falta Mowzie's Mobs para que existan), con el mismo comportamiento: son pasivos, huyen de los jugadores, se entierran si los acorralás, **solo se matan con un pico** y aparecen en cuevas (huevos de aparición en la pestaña **Cobinatio**).

| Cangrejo | Pasiva | Suelta | Aparece hasta la altura |
|---|---|---|---|
| Hierro | Aguanta 3 golpes de pico, casi inmune a otros daños | Hierro en bruto | Y 64 |
| Oro | Deslumbra (ceguera) a quien lo golpea | Oro en bruto | Y 32 |
| Cobre | Lanza un rayo al jugador (con aviso de 1 s para esquivarlo) | Cobre en bruto | Y 48 |
| Carbón | Lanza bolas de fuego | Carbón | Y 100 |
| Lapislázuli | Levanta rocas bajo el jugador (con aviso para esquivarlas) | Lapislázuli | Y 16 |
| Amatista | Espinas: lastima a quien lo golpea | Fragmentos de amatista | Y 40 |

- Los metales (hierro, oro, cobre) tienen el modelo liso y se animan suave; los cristales (lapislázuli, amatista) son puntiagudos y se animan a saltos; el carbón son trozos irregulares que tiemblan.
- Fortuna en el pico da más botín.
- Los ataques a distancia se pueden apagar en `config/mutantraisedead-common.toml` (`abilitiesEnabled`).
- Faltan (a propósito, por ahora): esmeralda, cuarzo, escombros antiguos, oro del Nether, etc.

## Cangrejo Gigante de Hierro (2x2)

- **No aparece solo.** En las cuevas (de Y -58 a 50) hay, muy raramente, cubos de 2x2x2 de "mena de hierro" que en realidad son un cangrejo dormido. Cerca de uno (unos 15 bloques) se escuchan rocas moviéndose.
- Al picar **cualquiera** de esos bloques, los demás desaparecen y el cangrejo sale del suelo (tarda 2 segundos y es invulnerable mientras sale).
- Tiene armadura, patas y tenazas de hierro en bruto. Solo se mata con **pico** y aguanta **20 golpes** (cambiable en `giantCrabHits`). Suelta mucho hierro en bruto y bloques de hierro en bruto.
- **Cada golpe suyo te aturde 1,5 segundos**: se invierten los controles de movimiento (efecto "Aturdido").
- En la pestaña **Cobinatio** del creativo están su huevo de aparición y los bloques nido (para probarlo sin buscarlo).

## Cómo conseguir el archivo .jar (gratis, sin instalar nada)

1. Creá una cuenta en https://github.com (gratis).
2. Arriba a la derecha tocá **+** → **New repository**. Ponele un nombre (ej. `mutant-revivir-muertos`) y tocá **Create repository**.
3. Tocá **uploading an existing file**. Descomprimí el zip y **arrastrá todo el contenido** de la carpeta (incluida la carpeta `.github`). Abajo tocá **Commit changes**.
   - Si la carpeta `.github` no se subió (en Mac y celular las carpetas con punto están ocultas): tocá **Add file → Create new file**, escribí como nombre `.github/workflows/compilar.yml`, pegá el contenido del archivo `compilar.yml` y tocá **Commit changes**.
4. Entrá a la pestaña **Actions**. Va a empezar solo a compilar (la primera vez tarda 5 a 10 minutos).
5. Cuando termine con tilde verde ✓, tocá la ejecución y abajo, en **Artifacts**, descargá `mutantraisedead-mod`. Adentro del zip está el `.jar`.
   - Si sale con cruz roja ✗: abrilo, copiá el error y pasámelo. Es normal que la primera versión necesite un ajuste.

## Instalación

Poné el `.jar` en la carpeta `mods` junto con Iron's Spells 'n Spellbooks 1.20.1-3.16.3 (y sus librerías), Mutant Monsters y Mutant More.
En un servidor va en el servidor **y** en la carpeta mods de cada jugador.
