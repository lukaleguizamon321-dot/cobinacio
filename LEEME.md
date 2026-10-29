# Mutant Raise Dead (Forge 1.20.1)

Combina **Revivir Muertos** (Iron's Spells 'n Spellbooks 3.16.3) con las químicas de **Mutant Monsters** y **Mutant More**.
No agrega items ni mobs nuevos: solo une lo que ya tenés.

## Cómo funciona en el juego

1. Tené un libro de hechizos con **Revivir Muertos al nivel máximo (6)**.
2. Ponelo en el **yunque arcano**: libro a la izquierda, la química a la derecha.
3. Sacá el resultado. El libro queda marcado y en la descripción dice *"Revivir Muertos Mutante - nivel N"*. La química se gasta.

| Química | Nivel | Probabilidad por cada zombi / esqueleto |
|---|---|---|
| Química X (Mutant Monsters) | 1 | 25 % |
| Fórmula Y (Mutant More) | 2 | 50 % |
| Compuesto Z (Mutant More) | 3 | 100 % |

4. Equipá el libro y lanzá Revivir Muertos. Cada zombi o esqueleto invocado puede salir como **Zombi Mutante** / **Esqueleto Mutante**, y ese mutante **te protege a vos** (no te ataca ni a otros jugadores).

Detalles:
- Solo sube de nivel (no se puede bajar ni repetir el mismo nivel).
- Los mutantes duran 10 minutos, te siguen, y atacan a los monstruos hostiles que te atacan o se acercan. No atacan jugadores, aldeanos, mascotas ni invocados.
- Se puede cambiar todo (probabilidades, duración, qué mutantes salen) en `config/mutantraisedead-common.toml` (se crea la primera vez que abrís el juego con el mod).

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
