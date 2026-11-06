# Cobinatio v1.9.0 (Forge 1.20.1)

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

Borrá el `.jar` viejo de la carpeta `mods` (ahora se llama `Cobinatio-1.8.0.jar`). Los libros marcados con la versión 1.0.0 ya no funcionan: hay que hacer el pergamino de nuevo con el yunque.

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
- Los de redstone, diamante y esmeralda (v1.7) están más abajo. Faltan (a propósito, por ahora): los del Nether (cuarzo, escombros antiguos, oro del Nether, etc.).

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

## v1.5 - Modelo nuevo de los cangrejos (Blockbench)
- El modelo de los 6 cangrejos sale de `cangry.bbmodel` (geometria en `crab/CrabMeshData.java`).
- Piezas moviles: cuerpo, caparazon, 4 patas y 2 pinzas. Las pinzas se levantan al usar una habilidad o al recibir un golpe, y el cangrejo se hunde al enterrarse.
- Las texturas `textures/entity/grottol_*.png` son una hoja de 128x128. El cuerpo dorado es igual en todos; lo que cambia por mineral es el caparazon (el cuadradito del mineral arriba del caparazon) y las patas (cobre, oro y carbon tienen las suyas). Lapislazuli y amatista usan el modelo de cristal (`cangry`/`model.bbmodel`, cristales en la espalda) con el mismo caparazon de roca y las mismas patas lisas.

## v1.6 - Cangrejo gigante nuevo (Blockbench). Ningun cangrejo recibe dano de fuego ni de caida
- El cangrejo gigante de hierro usa `cangrejo_gigante.bbmodel` (geometria en `CrabMeshData.GIANT`): caparazon de 2x2 de mineral, patas con mineral al frente y tenazas de bloque de hierro en bruto.
- Mantiene lo de antes: sale del suelo (se hunde y emerge), las tenazas golpean al atacar, 20 golpes de pico.
- Textura: `textures/entity/giant_iron_crab.png` (256x128, junta todas las texturas del modelo).

## v1.7 - Cangrejos de redstone, diamante y esmeralda + efectos + logros

Los tres usan el modelo de cristal con la textura de su bloque. **No se entierran** (los 6 anteriores siguen enterrándose).

| Cangrejo | Qué hace | Suelta | Aparece |
|---|---|---|---|
| Redstone | Al golpearlo te da **Detectado** (5 min): los monstruos hasta 25 bloques te detectan aunque haya paredes en el medio | Redstone | Cuevas hasta Y 16 |
| Diamante | **Ataca** al jugador. Si lo matás te da **Desafortunado** (3 min) | Diamante | Cuevas hasta Y 16 (muy raro) |
| Esmeralda | Amigable: a los aldeanos que tenga a menos de 10 bloques les baja los precios hasta un 25 %. Te sigue si llevás una esmeralda en la mano y se queda cerca de los aldeanos | Esmeralda | Cuevas de montañas y colinas |

- **Detectado**: no afecta a mobs neutrales (endermans, piglins zombificados...), a los mutantes tuyos ni a las plantas invocadas.
- **Desafortunado** = Fortuna al revés: al picar una mena (sin toque de seda) cada item que suelta tiene un 50 % de perderse, y además baja la suerte (pesca, cofres).
- Los valores (segundos, rango, porcentaje) se cambian en `config/mutantraisedead-common.toml`. Si preferís que el de diamante no ataque y solo dé Desafortunado al morir, poné `diamondCrabAttacks = false`.

### Pestaña de logros "Cobinatio" (tecla L)
- **Karma**: pegarle a un cangrejo de diamante.
- **¡Uhh, brillante!**: recibir la ceguera del cangrejo de oro.
- **Todos te vemos**: recibir Detectado del cangrejo de redstone.
- **Tan duro como el acero**: que se te rompa un pico peleando contra un cangrejo de hierro (el normal o el gigante).
- **Mago eléctrico nivel 15**: morir por un rayo del cangrejo de cobre.
- **¡Qué arde!**: matar a un cangrejo de carbón sin que ninguno de sus disparos te pegue.
- **Ayuda comercial**: que un mismo cangrejo de esmeralda le haga descuento a 15 aldeanos distintos.
- **Mucho azul**: matar a un cangrejo de lapislázuli.
- **Violencia violeta** (invento mío): golpear a un cangrejo de amatista y comerte sus espinas.
- **Esta vaina es seria bro** (v1.8): haber tenido todos los hechizos con mutantes (Revive Mutantes X, Y y Z, Cabeza de Creeper Mutante e Invocar Blaze Mutante X e Y). Cuenta lo que llevás en el inventario y en el libro de hechizos; lo que ya viste queda anotado, no hace falta tenerlos todos juntos.

## v1.8 - Hechizos nuevos: Invocar Cangrejo, Cabeza de Creeper Mutante, Invocar Blaze

Todos funcionan como Revivir Muertos: lo que invocan **te protege**, dura 10 minutos, no suelta botín ni experiencia, y si volvés a lanzar el hechizo los despedís.

### Invocar Cangrejo (carbón, cobre, hierro) — 5 niveles
1. **Mesa de inscripción de pergaminos** (la de Iron's): **tinta común + papel + una esmeralda** = *Invocar Cangrejo de Carbón* nivel 1 (1 cangrejo). La esmeralda va en el espacio del foco, igual que con los hechizos de evocación.
2. **Yunque arcano**: pergamino a la izquierda y tinta a la derecha, en orden: **poco común** = nivel 2 (2 cangrejos), **rara** = nivel 3 (3), **épica** = nivel 4 (4), **legendaria** = nivel 5 (4 cangrejos con 25 de vida en vez de 20).
3. **Cambiar de material**: pergamino de carbón **nivel 5** + **bloque de cobre** en el yunque = *Invocar Cangrejo de Cobre* nivel 1 (se reinicia y vuelve a subir con la misma tinta). Pergamino de cobre nivel 5 + **bloque de hierro** = *Invocar Cangrejo de Hierro* nivel 1.
- Qué hace cada uno: **carbón** dispara bolas de fuego, **cobre** lanza rayos (solo lastiman a monstruos hostiles, nunca a vos), **hierro** pelea cuerpo a cuerpo y tiene armadura.
- Los cangrejos invocados **reciben daño normal** de cualquier arma o mob (no solo del pico) y ya no huyen ni se entierran.
- Config: `requiredCrabLevel` (nivel necesario para cambiar de material, 5) y `crabLevel5Health` (25).

### Cabeza de Creeper Mutante — 1 nivel
- Pergamino de **Cabeza de Creeper** (Iron's) en nivel máximo (10) + **Química X** en el yunque arcano = *Cabeza de Creeper Mutante*. Invoca **un** Creeper Mutante (Mutant Monsters) que te protege.
- Sus explosiones **no rompen bloques** ni te lastiman a vos ni a tus aliados.

### Invocar Blaze (fuego) — 2 niveles
1. **Mesa de inscripción** con el foco de fuego: **tinta épica** = nivel 1 (2 blazes). En el yunque arcano, **tinta legendaria** = nivel 2 (4 blazes).
2. **Versiones mutantes**: pergamino de Invocar Blaze nivel 2 + **Química X** = *Invocar Blaze Mutante (X)* (25 % de cada blaze sale mutante). Con **Fórmula Y** = *(Y)* (50 %).
3. Los blazes mutantes son el **Blaze Mutante de Mutant More**. **Máximo 2 mutantes** por lanzamiento: si en la tirada salen más, el juego elige cuáles quedan como blaze normal.
- Si el id del Blaze Mutante de tu versión de Mutant More es otro, se cambia en el config (`blazeMutants`). Si el id no existe, salen blazes normales.
- Config: `blazeChanceX`, `blazeChanceY`, `maxMutantBlazes`, `requiredBlazeLevel`, `requiredCreeperLevel`.

Los pergaminos para probar están en la pestaña **Cobinatio** del creativo.

## v1.9 - La guarida de los cangrejos

Una estructura subterránea nueva, como las mazmorras: se genera sola, enterrada entre las alturas -50 y 30, en cualquier bioma del mundo normal (no aparece bajo agua ni lava, y es bastante rara).

- **Zona central**: sala redonda (cilindro) de **radio 10 y 9 bloques de alto**, con piso en anillos y paredes de ladrillo de piedra (algunos con musgo o agrietados) si la guarida está por encima de Y=0; por debajo de Y=0 es de ladrillo de pizarra profunda y los faroles son de almas.
- **Pedestal** en el medio de la sala. Por ahora no hace nada; queda reservado para lo que sigue (jefes y huevos).
- **9 nidos pegados a la pared**, uno por cada cangrejo, en este orden: carbón, hierro, cobre, oro, lapislázuli, amatista, redstone, diamante y esmeralda. Son cubos de 2x2x2 que parecen mena de ese material. Entre nido y nido hay un **pilar con una linterna**.
- **Puerta** de 3x4 en uno de los cuatro lados (norte, sur, este u oeste); de afuera hay que cavar o cruzar una cueva para llegar a ella.
- **Los nidos**: al romper cualquiera de los bloques, todo el cubo desaparece y **despierta el cangrejo** de ese material. El de hierro despierta al **cangrejo gigante**; los otros, un cangrejo normal (el de diamante va directo a pelear). Los nidos se pueden conseguir en el creativo (pestaña Cobinatio): *Mena de X (nido de cangrejo)*; se rompen con pico de piedra o mejor y no sueltan nada.
- **Probarla sin buscarla** (hace falta ser operador, nivel 2): escribí `/cobinatio guarida`. La construye alrededor de donde estás parado y te deja adentro, al lado del pedestal. Si lo hacés en la superficie, queda como un búnker a medio enterrar; para verla bien enterrada, usalo dentro de una cueva o cavando hacia abajo antes.
- También sirve el comando de Minecraft `/place feature mutantraisedead:crab_lair` (ahí no se hacen las comprobaciones de la generación normal).

Qué viene después (todavía NO está): un jefe por cada cangrejo que se pelea en la sala central, y huevos de cangrejo que crecen sobre su mena y dan un cangrejo aliado.

