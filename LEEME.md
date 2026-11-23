# Cobinatio v1.20.0 (Forge 1.20.1)

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

- **Zona central**: sala redonda (cilindro) de **radio 15 y 14 bloques de alto** (en la v1.9 era de radio 10 y 9 de alto; desde la v1.14 es más grande, ver más abajo), con piso en anillos y paredes de ladrillo de piedra (algunos con musgo o agrietados) si la guarida está por encima de Y=0; por debajo de Y=0 es de ladrillo de pizarra profunda y los faroles son de almas.
- **Pedestal** en el medio de la sala. Por ahora no hace nada; queda reservado para lo que sigue (jefes y huevos).
- **9 nidos pegados a la pared**, uno por cada cangrejo, en este orden: carbón, hierro, cobre, oro, lapislázuli, amatista, redstone, diamante y esmeralda. Son cubos de 2x2x2 que parecen mena de ese material. Entre nido y nido hay un **pilar con una linterna**.
- **Puerta** de 3x4 en uno de los cuatro lados (norte, sur, este u oeste); de afuera hay que cavar o cruzar una cueva para llegar a ella.
- **Los nidos**: al romper cualquiera de los bloques, todo el cubo desaparece y **despierta el cangrejo** de ese material. El de hierro despierta al **cangrejo gigante**; los otros, un cangrejo normal (el de diamante va directo a pelear). Los nidos se pueden conseguir en el creativo (pestaña Cobinatio): *Mena de X (nido de cangrejo)*; se rompen con pico de piedra o mejor y no sueltan nada.
- **Probarla sin buscarla** (hace falta ser operador, nivel 2): escribí `/cobinatio guarida`. La construye alrededor de donde estás parado y te deja adentro, al lado del pedestal. Si lo hacés en la superficie, queda como un búnker a medio enterrar; para verla bien enterrada, usalo dentro de una cueva o cavando hacia abajo antes.
- También sirve el comando de Minecraft `/place feature mutantraisedead:crab_lair` (ahí no se hacen las comprobaciones de la generación normal).

## v1.9.1 - Skins nuevas de pergaminos

- **Invocar Cangrejo** ya no usa una sola imagen: cada hechizo tiene su pergamino. El de **carbón** tiene marcas negras, el de **cobre** marcas verde agua y naranja, y el de **hierro** marcas rosadas claras.
- **Invocar Blaze** (el normal, de fuego) usa el pergamino rojo con la cara. Los de Blaze Mutante (X, Y) siguen con el pergamino oscuro de las mutaciones.
- Es solo visual: se ve igual en el inventario, en la mano y tirado en el suelo.
- Si ves cuadrados violeta y negro en la pestaña Cobinatio o cangrejos violeta y negros, es que faltan archivos de imágenes en tu repo (la carpeta `src/main/resources/assets`). Se arregla subiendo esa parte de nuevo.

## v1.11 - Doble Almacén (artefacto): el inventario crece

Un amuleto nuevo para la ranura de **amuletos (charm) de Curios**, pensado para convivir con el mod **Artifacts**. Mientras lo llevás puesto, **tu inventario se agranda como un cofre doble**: al apretar la tecla del inventario (E) aparecen **27 espacios más debajo** de tu inventario normal. No se abre ninguna ventana aparte (en la v1.10 era una ventana con la tecla B; esa tecla ya no existe).

- **Cómo se usa:** ponete el amuleto en la ranura de amuletos de Curios y abrí el inventario con la tecla de siempre. Abajo vas a ver el panel "Doble Almacén" con 3 filas de 9. Se usa igual que un cofre: arrastrar, clic derecho para repartir, etc.
- **Dónde se guardan las cosas:** adentro del propio amuleto (como una caja de shulker). Si te lo sacás, los espacios desaparecen y las cosas se van con el amuleto; si te lo ponés de nuevo, siguen ahí. Si morís, el amuleto se comporta como cualquier otro objeto tuyo (cae o no según tu gamerule `keepInventory`).
- **Inventario lleno:** si el amuleto está puesto y tu inventario no tiene lugar, lo que recojas del suelo se guarda solo en los espacios extra. Se apaga con `overflowPickup=false` en el config.
- **Dónde se consigue:** los **mímicos** de Artifacts lo sueltan con 5% de probabilidad y los cofres de estructuras (minas abandonadas, ciudades antiguas, templos, fortalezas, mansiones, bastiones, etc.) lo traen con 4%. En el creativo está en la pestaña Cobinatio y también en la pestaña de Artifacts.
- **Para probarlo:** ponete en **modo supervivencia** (`/gamemode survival`). El inventario del modo creativo es otra pantalla distinta de Minecraft y ahí los espacios extra no se muestran (las cosas guardadas siguen a salvo en el amuleto).
- **Limitación conocida:** con shift+clic desde tu inventario las cosas se mueven entre la mochila y la barra rápida como siempre (no saltan solas al espacio extra); para llenarlo se arrastra, o se llena solo al recoger cosas con el inventario lleno.
- **Textura:** por ahora usa la caja de shulker violeta como imagen provisoria. Cuando tengas tu textura, reemplazá el archivo `src/main/resources/assets/mutantraisedead/textures/item/doble_almacen.png` (16x16) y subilo.
- Hace falta **Curios** (ya lo pide Iron's Spells). **Artifacts** es opcional: sin él, el amuleto igual funciona, pero no hay mímicos para conseguirlo.
- **v1.11.1 - la ranura de amuletos viene con Cobinatio:** Curios por sí solo define el tipo de ranura "charm" (amuleto) pero no se la da al jugador; eso lo hacía otro mod. Ahora Cobinatio se la da al jugador por su cuenta (archivo `data/mutantraisedead/curios/entities/doble_almacen.json`), así que alcanza con tener **Curios** instalado. Curios muestra sus ranuras junto a la pantalla del inventario (como cuadritos a la izquierda o con un botón que abre su propia pantalla, según su configuración); ahí vas a ver la de amuleto.

## v1.12 - Jefe: Cangrejo Titán de Carbón

En el centro de cada guarida, sobre el pedestal, ahora hay un **huevo**. Mientras nadie se acerca, duerme. Cuando un jugador se le acerca (7 bloques) o lo toca con clic derecho, **el huevo desaparece, el pedestal del centro se destruye y sale el jefe**.

- **Tamaño:** 3 bloques de ancho y 6 de alto. Al despertar sale del suelo rugiendo (unos 3 segundos) y mientras tanto no recibe daño.
- **Vida:** 300 (150 corazones). Se cambia en el config con `coalBossHealth`. Tiene barra de vida del jefe arriba en la pantalla.
- **Ataques (son tres, nada más):**
  1. **Bolas de fuego:** un abanico de 3 bolas hacia vos (5 si está furioso), como el cangrejo de carbón chico pero de a varias.
  2. **Embestida:** se queda un momento temblando (aviso para que te corras), después corre en línea recta y se lleva puesto a quien se cruce. Si choca contra una pared se aturde unos segundos: es el momento para pegarle.
  3. **Llamarada:** prepara el fuego y escupe un cono de llamas hacia adelante que va girando despacio hacia vos. Se esquiva corriendo hacia un costado. Quema a quien alcance.
- **Furioso:** con menos de la mitad de vida espera menos entre ataque y ataque, y la embestida es más rápida.
- **No le hace daño el fuego ni la caída.** A diferencia de los cangrejos chicos, a este se lo puede matar con cualquier arma o hechizo, no hace falta un pico.
- **Botín:** al morir suelta **bloques de carbón** (de 12 a 24, más con Saqueo), experiencia y, desde la v1.14, el **Huevo de Cangrejo de Carbón con imperfecciones** (siempre uno; ver la v1.14).
- **El huevo de la guarida:** en supervivencia no se puede picar (si no, te saltabas al jefe). Para probar el jefe en creativo usá el huevo de aparición o `/cobinatio guarida`. (Desde la v1.14 un huevo puesto a mano ya no invoca al jefe, ver abajo.)
- **Para probarlo:** `/cobinatio guarida` crea una guarida y te deja adentro, lejos del huevo. Caminá hacia el pedestal del centro para despertar al jefe. El alcance se cambia con `bossWakeRange`.
- **Modelo:** desde la v1.13.0 usa tu modelo propio (ver más abajo).
- **El huevo (v1.12.1):** ya usa tu modelo y tu textura de Blockbench (`models/block/coal_crab_egg.json` y `textures/block/coal_crab_egg.png`), y tu dibujo como ícono en el inventario (`textures/item/coal_crab_egg.png`). La caja de golpes del huevo mide lo mismo que el modelo (8 de ancho y 11 de alto).
- Es el primer jefe; los de los otros materiales se hacen después con esta misma base.

## v1.13.0 - El jefe ya tiene su modelo de Blockbench

El Cangrejo Titán de Carbón ahora se ve con **tu modelo y tu textura** (`boss.bbmodel`): caparazón de cubos de carbón, cuerpo marrón con ojos rojos, 4 patas grandes y 2 pinzas enormes.

- **Tamaño:** el modelo, tal como lo hiciste, mide 5,25 bloques de alto (del suelo al techo del caparazón). Lo agrandé parejo **1,14 veces** para que mida **6 bloques de alto**. El caparazón queda de unos 4,7 de ancho y, con las patas abiertas, el bicho se ve de unos 12 bloques de punta a punta. La **caja de golpes** sigue siendo de **3 de ancho x 6 de alto** (lo que se golpea es el cuerpo del centro; las patas y las pinzas son solo visuales). Entraba justo en la sala de la v1.9 (radio de 10 y 9 de alto); por eso en la v1.14 la guarida es más grande. Si lo querés más grande o más chico, se cambia un solo número (`SCALE` en `CoalCrabBossRenderer.java`); si querés la caja más ancha, se cambia el `sized(3.0F, 6.0F)` del jefe en `ModEntities.java`.
- **Se mueve igual que antes:** las 4 patas caminan en diagonal, las pinzas se levantan al rugir y al lanzar fuego, se abren al prepararse para la embestida, se juntan al embestir y caen cuando se aturde; el caparazón tiembla. Al despertar sale del suelo.
- **Las bolas de fuego y la llamarada** ahora salen de la boca del modelo (2,6 bloques adelante y 3,9 de alto).
- **Textura:** `textures/entity/coal_crab_boss.png` (512x512) es la primera que me mandaste ("textura para el boss de carbón", negra con grietas y ojos naranjas). Encaja exacto con las caras del modelo. La textura de la segunda versión del modelo (la de colores pastel a medio pintar) no se usa.
- **Cosas del modelo que ajusté:** (1) un cubo suelto de color celeste que estaba **oculto** en el modelo (a un costado del caparazón) no se incluye, igual que en Blockbench. (2) Las caras que quedan pegadas entre cubos (tapadas por dentro) se sacan, para que el modelo sea más liviano. (3) Las patas y las pinzas de tu modelo estaban un poco por debajo del suelo (las puntas de las pinzas, unos 5 píxeles); las pinzas en reposo se suben un poquito para no atravesar el piso.

## v1.14.0 - Guarida más grande y el huevo que nace

**1) La guarida es más grande.** El jefe no entraba bien en la sala, así que ahora es mucho más amplia:
- Sala redonda de **radio 15 y 14 bloques de alto** (antes 10 y 9). Puerta de 5 de ancho por 6 de alto. Los nidos y los pilares con farol quedaron pegados a la pared nueva, y el piso tiene un círculo central más grande con el pedestal en el medio.
- Al generarse con el mundo, el centro de la sala queda en el medio del chunk (Minecraft solo deja construir hasta 16 bloques fuera del chunk y la sala llega a 17,5 desde el centro).
- Las guaridas que ya estaban en tu mundo siguen siendo chicas; solo las nuevas (o las del comando `/cobinatio guarida`) salen grandes. Para ver una grande, creá un mundo nuevo o usá el comando.

**2) El jefe suelta el huevo.** Al morir, además de los bloques de carbón, suelta siempre el **Huevo de Cangrejo de Carbón con imperfecciones** (usa el dibujo que hiciste). Ese huevo no se puede poner todavía: hay que pulirlo.

**3) Piedra de afilar.** Poné el huevo con imperfecciones **solo** en la piedra de afilar (en cualquiera de las dos ranuras, la otra vacía) y sale el **Huevo de Cangrejo de Carbón pulido** (sin imperfecciones; le armé un dibujo liso a partir del tuyo, si querés cambiarlo es `textures/item/coal_crab_egg.png`), más un poquito de experiencia.

**4) Incubar.** El huevo pulido solo se puede poner **arriba de un bloque de carbón** (si lo intentás en otro lado, avisa y no se coloca). Si le sacás el bloque de abajo, se suelta como objeto.
- Tarda **10 minutos** en incubar (cuenta mientras el lugar esté cargado, o sea, cerca de algún jugador). Se cambia con `eggIncubationSeconds` en el config (para probar poné 10).
- Mientras incuba suelta chispitas. Con **clic derecho** te dice cuánto falta.
- Cuando se cumple el tiempo, espera a que haya un jugador a **menos de 5 bloques**: el **primero** que esté cerca es el **dueño**. El huevo desaparece y nace un **cangrejo de carbón chico aliado**.
- El cangrejo aliado **no desaparece** (a diferencia de los invocados con el hechizo): te sigue, te protege tirando bolas de fuego a los monstruos hostiles, nadie lo puede lastimar a propósito ni vos ni otros jugadores, no suelta botín y si te desconectás se queda esperándote. Es la versión chica (20 de vida).
- Puede romperse con la mano (devuelve el huevo pulido).

**5) Huevos naturales vs. huevos puestos.** Solo el huevo **natural** del pedestal de la guarida invoca al jefe (se acerca un jugador o lo toca y despierta). Un huevo puesto por un jugador **nunca** invoca al jefe: solo incuba el cangrejo chico.

Cosas a tener en cuenta:
- Por ahora está solo el huevo de **carbón**; los de los otros materiales (cada uno sobre su bloque: hierro, oro, cobre, etc.) vendrán con los otros jefes.
- Los huevos de la v1.12/v1.13 que ya estaban puestos en la guarida siguen funcionando igual (son los naturales).
- Si el huevo suelto que ponías en creativo para invocar al jefe te servía para probar, usá el huevo de aparición del jefe.

Qué viene después (todavía NO está): los jefes de los demás cangrejos (cada uno con su huevo, su bloque de incubación y su botín) y el resto del botín del jefe de carbón.

## v1.15.0 - El cangrejo de carbón con horno

La mascota de carbón (la que nace del huevo pulido) ahora puede llevar un **horno encima** y cocinar o fundir cosas **sin combustible**: el cangrejo pone el calor.

**Cómo se usa**
1. Tené un **horno** en la mano (el bloque común) y hacele **clic izquierdo** (un golpe) a tu cangrejo de carbón mascota. El golpe **no le hace daño**: el horno se queda puesto encima del caparazón (se gasta uno, salvo en creativo). Solo el **dueño** puede ponerlo, y solo una vez por cangrejo.
2. **Clic derecho** al cangrejo: se abre la ventana del horno, **sin la ranura del carbón** (se tapa y queda solo la llama, que se prende mientras cocina). Quedan la ranura de arriba (lo que querés cocinar) y la de resultado.
3. Cocina con **todas las recetas del horno común** (minerales en bruto, comida, arena a vidrio, etc.), a la misma velocidad que un horno normal, mientras el cangrejo esté vivo y cargado en el mundo. Aunque cierres la ventana sigue cocinando. Al sacar el resultado te da la **experiencia** acumulada, como en un horno.
4. Con el shift-clic las cosas van solas a la ranura de arriba (si se pueden cocinar) o a tu mochila.

**Cómo se ve**
- Se dibuja **solo la mitad de arriba del horno** (la parte de la boca, sin el cenicero de abajo), apoyada sobre el caparazón y **inclinada igual que la tapa del caparazón** (15 grados), de modo que queda pegada. Se mueve con el caparazón cuando respira o camina.
- Usa las texturas del **horno del juego** (si tenés un paquete de texturas, se ve con las tuyas), y la ventana también usa el dibujo del horno de tu paquete.
- Mientras cocina, la boca del horno se ve **prendida**, larga **humo** por arriba y chispas por adelante, y se oye el crepitar del fuego.

**Si el cangrejo muere**
- Suelta el **horno**, lo que tenía adentro (lo que se cocinaba y el resultado) y la experiencia que había juntado. (Los cangrejos aliados no sueltan botín, por eso lo hace el propio horno.)
- El horno **se guarda** con el cangrejo: si salís del mundo y volvés, sigue puesto con todo lo que tenía adentro.

**Cosas a tener en cuenta**
- Solo la **mascota de carbón del huevo** puede llevar horno (no los cangrejos de carbón salvajes, ni los invocados con el hechizo).
- Por ahora el horno **no se puede sacar** sin que el cangrejo muera.
- No lleva el librito verde de recetas del horno común (no hacía falta); el resto de la ventana es igual.
- Es el **horno común**: el alto horno y el ahumador (que son otros bloques) no se pueden poner todavía.

Archivos nuevos: `crab/CrabFurnaceLayer.java` (dibujo), `crab/CrabFurnaceMenu.java` y `crab/CrabFurnaceScreen.java` (ventana), `crab/CrabFurnaceEvents.java` (poner el horno con el golpe), `crab/CrabFurnaceClient.java` y `crab/ModMenus.java` (registro). Si querés mover el horno o cambiarle el tamaño, los números están al principio de `CrabFurnaceLayer.java` (`CX`, `CY`, `CZ`, `HALF`, `HEIGHT`).

## v1.16.0 - Jefe: Cangrejo Titán de Cobre (y su guarida)

Segundo jefe del mod, con el mismo proceso que el de carbón: duerme como un huevo en el centro de **su propia guarida**, suelta su huevo, el huevo se pule y se incuba. (En la v1.17.0 pasó a usar el modelo grande del jefe de carbón con tu textura de cobre; ver la sección de abajo. La caja que se golpea es de 3 de ancho por 6 de alto.)

**Cómo es el jefe**
- 300 de vida (150 corazones; `copperBossHealth` en el config), armadura 8, no se mueve con los golpes, inmune al fuego, a las caídas y a los rayos. Tiene barra de jefe amarilla. Al despertar sale del suelo rugiendo (3 segundos, invencible).
- **Golpe normal** cuando estás cerca (10 de daño).
- **Embestida**: se prepara (pinzas abiertas, chispas en el suelo), corre en línea recta y se lleva puesto a quien se cruce (14 de daño y lo manda a volar). Si choca con una pared o una columna **se aturde unos 2,5 segundos** (ahí es cuando se lo ataca). Con menos de la mitad de vida es más rápida.
- **Rayos de colores** (el ataque principal, ver abajo). Con menos de la mitad de vida espera menos entre ataques, junta energía más rápido y tira **dos rayos** a la vez (uno donde estás y otro adonde vas corriendo).

**Los rayos de colores**
Primero levanta las pinzas y le salen **partículas del color del rayo que va a tirar**, así sabés cuál viene. Un poco antes de que caiga marca en el suelo un **círculo de ese color** (queda fijo donde estabas: hay casi un segundo para salir corriendo). El rayo cae en un área de unos 2,4 bloques de radio. El daño base es 4 corazones (`copperBoltDamage`).
- **Blanco**: el normal, solo daño.
- **Violeta**: **Electrocución** (efecto nuevo). Una descarga violeta cada 2 segundos, de 1 corazón cada una (daño mágico, la armadura no lo frena). Dura **30 segundos**; con armadura de metal sube hasta **1 minuto**.
- **Amarillo**: **Parálisis** (efecto nuevo). No te podés mover ni saltar. Dura **10 segundos**; con armadura de metal hasta **15**.
- **Rojo**: daño **5% mayor** que el blanco; con armadura de metal, **10%**.
- **Azul**: **Maná anulado** (efecto nuevo). Tu maná queda en 0 y no podés lanzar hechizos de Iron's Spells. Dura 20 segundos (`manaLockSeconds`). Cuando se te acaba el efecto el maná vuelve a regenerarse normal.
- **Verde**: **no te lastima**. El rayo cae sobre el propio jefe y le **cura el 5% de su vida**. Si un jugador está **pegado al jefe** (a menos de 4 bloques), también recibe el rayo y **recupera 4 corazones**. Solo sale cuando el jefe tiene menos del 90% de vida.

**Armadura de metal** = hierro, oro, cota de malla y netherita. Cuenta cuántas piezas llevás (0 a 4) y el efecto crece parejo: con 2 piezas dura la mitad de la diferencia. La lista está en `data/mutantraisedead/tags/items/conductive_armor.json` (se pueden agregar armaduras de otros mods).

Todos los tiempos y daños se cambian en el config (`config/mutantraisedead-common.toml`, sección `cangrejos`).

**Botín**: al morir suelta de **12 a 24 bloques de cobre** (más con Looting) y siempre el **Huevo de Cangrejo de Cobre con imperfecciones**.

**El huevo de cobre** (igual que el de carbón)
1. Se pule **solo** en la piedra de afilar y sale el **Huevo de Cangrejo de Cobre pulido**.
2. Solo se puede poner **arriba de un bloque de cobre** (vale el cobre común, expuesto, desgastado u oxidado, y los encerados). En otro lado avisa y no se coloca.
3. Incuba lo mismo que el de carbón (`eggIncubationSeconds`, 10 minutos); cuando se cumple, el primer jugador a menos de 5 bloques es el dueño y nace un **cangrejo de cobre chico aliado** (te sigue, te protege con sus rayos, nadie lo lastima, no suelta botín).
4. Los dibujos del huevo (bloque, pulido y con imperfecciones) son los de carbón **pintados color cobre** con unas manchitas verdes de óxido; si querés cambiarlos son `textures/block/copper_crab_egg.png`, `textures/item/copper_crab_egg.png` y `textures/item/copper_crab_egg_rough.png`.

**La guarida de cobre** (nueva, distinta a la de carbón)
- Misma sala redonda grande (radio 15, 14 de alto, puerta de 5 por 6, enterrada en las cuevas), pero **toda de cobre**: cobre cortado encerado con tramos expuestos, desgastados y oxidados (verdosos). El piso tiene un anillo verde oxidado.
- En el centro, el pedestal con el **huevo del jefe de cobre**.
- **9 nidos de cangrejo de cobre** en la pared (en la de carbón hay uno de cada material).
- Entre los nidos hay pilares con una **luz verde** (rana verde) y un **pararrayos** arriba.
- **4 columnas de cobre con pararrayos** en la sala: sirven para taparte de los rayos y, si el jefe se las lleva por delante en una embestida, se aturde.
- Se genera sola en el mundo (un poco más rara que la de carbón, 1 cada 40 chunks, en las cuevas). Para probarla: `/cobinatio guarida cobre` (la de carbón es `/cobinatio guarida` o `/cobinatio guarida carbon`).

**En la pestaña creativa**: Huevo de aparición del jefe de cobre, Huevo de Cangrejo de Cobre con imperfecciones y pulido.

Cosas a tener en cuenta
- No pude probar el mod: lo escribí leyendo cómo funcionan las clases de Minecraft. El efecto **Maná anulado** usa la API de Iron's Spells (`MagicData.setMana`); si el compilador se queja de eso, pasame el error.
- Las guaridas de carbón que ya estaban en tu mundo no cambian. La de cobre solo aparece en chunks nuevos o con el comando.
- Todavía no hay cangrejos del Nether ni los jefes de los otros materiales.

Archivos nuevos: `crab/CopperCrabBossEntity.java` (el jefe y sus ataques), `crab/CopperCrabBossModel.java` y `crab/CopperCrabBossRenderer.java` (dibujo), `crab/ColoredBoltEntity.java` y `crab/ColoredBoltRenderer.java` (los rayos), `crab/BoltColor.java`, `crab/MetalArmor.java`, `crab/ElectrocutionEffect.java`, `crab/ParalysisEffect.java`, `crab/ManaLockEffect.java`, `crab/EffectEvents.java`. Cambiados: `CrabEggBlock` (ahora sirve para carbón y cobre), `CrabEggBlockEntity`, `PolishedEggItem`, `RoughEggItem`, `EggEvents`, `ModBlocks`, `ModEntities`, `ModEffects`, `LairFeature` (dos temas), `LairCommand`, `ClientSetup`, `ClientEvents`, `CrabEvents`, `ModRegistry`, `Config`.

## v1.17.0 - Jefe de cobre con su textura y su música, y la mascota de cobre con pararrayos

**1) El jefe de cobre usa el modelo grande.** Ahora el Cangrejo Titán de Cobre usa **el mismo modelo del jefe de carbón** (el de `boss.bbmodel`), con **tu textura** de cobre (`textures/entity/copper_crab_boss.png`; tiene el mismo dibujo de piezas que la de carbón, así que si la querés cambiar, es ese archivo). Mide 6 de alto y la caja que se golpea es de 3 por 6, igual que el de carbón. Las partículas del color del rayo ahora salen de las puntas de las pinzas grandes (cuando las levanta para juntar el rayo).

**2) La música del jefe.** Cuando el jefe aparece (sale del huevo) empieza a sonar **"Colossus Beneath the Crags"** y sigue mientras esté vivo; cuando termina la canción vuelve a empezar. Se escucha hasta **15 bloques a la redonda**: a menos de 6 bloques de él suena a todo volumen y de ahí baja de a poco hasta apagarse a los 15. Cuando el jefe muere se corta. Mientras suena, la música de fondo del juego se calla.
- El archivo es `assets/mutantraisedead/sounds/copper_boss_music.ogg` (convertí tu mp3 al formato que usa Minecraft, ogg). Si algún día querés cambiar la canción, reemplazá ese archivo (tiene que ser .ogg).
- La música usa el control de volumen de **Música** de las opciones del juego.

**3) Mascota de cobre con pararrayos** (la que nace del huevo pulido de cobre)
1. Con un **pararrayos** en la mano, **clic izquierdo** (un golpe) a tu cangrejo de cobre mascota. El golpe no le hace daño: el pararrayos se queda parado arriba del caparazón (se gasta uno, salvo en creativo). Solo el dueño puede ponerlo, y una vez por cangrejo.
2. Ya con el pararrayos puesto, cada **clic izquierdo** tuyo al cangrejo **cambia de rayo**: verde → violeta → amarillo → verde... (como sentar y seguir a un perro). Te avisa en pantalla cuál quedó elegido y de la punta del pararrayos salen partículas de ese color.
3. Con el pararrayos puesto el cangrejo **deja de tirar el rayo común** y usa **solo el rayo elegido** (uno por vez), con una espera entre rayo y rayo (8 segundos, `petRodCooldownSeconds`):
   - **Verde**: cura. Cuando vos, el cangrejo o alguna otra mascota tuya que esté a **menos de 7 bloques** están heridos, el cangrejo prepara el rayo (se abre un círculo verde en el piso hasta los 7 bloques) y al caer **cura 3 corazones a todos**: a sí mismo, a vos y a las otras mascotas. No ataca.
   - **Violeta**: el rayo cae sobre el monstruo hostil que está atacando (marca un círculo antes) y le hace **daño + Electrocución** (10 segundos).
   - **Amarillo**: igual pero con **Parálisis** (8 segundos).
4. Si el cangrejo muere, suelta el pararrayos. Se guarda con el cangrejo (si salís del mundo y volvés, sigue puesto y con el rayo elegido).
5. Solo la **mascota del huevo** puede llevar pararrayos (no los cangrejos de cobre salvajes ni los invocados con el hechizo).

Todo se puede cambiar en el config (`petRodHealRadius`, `petRodHeal`, `petRodCooldownSeconds`, `petRodBoltDamage`, `petRodElectrocutionSeconds`, `petRodParalysisSeconds`).

Cosas que supuse (decime si lo querías distinto)
- "Uno por rotada" lo entendí como que con cada clic izquierdo elegís uno de los 3 y ese es el único que usa hasta que lo cambies. Si lo querías automático (que el cangrejo vaya rotando solo de un color al otro cada vez que tira), se cambia fácil.
- El verde solo se tira si alguien necesita curación; no se gasta de balde.

Archivos nuevos: `crab/BossMusicClient.java` (música), `crab/ModSounds.java`, `crab/CrabRodLayer.java` (dibujo del pararrayos), `crab/CrabRodEvents.java` (poner y cambiar con el clic izquierdo). Cambiados: `CopperCrabBossModel/Entity/Renderer` (modelo grande), `GrottolVariantEntity` (pararrayos y rayos), `GrottolVariantRenderer`, `ModEntities`, `MutantRaiseDead`, `Config`, `sounds.json`.

## v1.18.0 - Jefe: Cangrejo Titán de Amatista (con su guarida geoda), y las mascotas se pueden sentar

**1) El jefe.** Usa **tu modelo** (`boss_amatista_panza_piedra_arreglada.bbmodel`) con **tu textura** (`textures/entity/amethyst_crab_boss.png`, 512 x 512; si la querés cambiar, es ese archivo). Mide unos 8 bloques de alto con los cristales de la espalda y unos 11 de ancho con las patas abiertas; la caja que se golpea es de 3,6 de ancho por 6,4 de alto. Duerme como un huevo en el centro de su guarida, y cuando te acercás despierta (sale del suelo y ruge, 3 segundos sin poder lastimarlo). Tiene barra de vida violeta, 300 de vida (150 corazones) y no recibe daño de fuego ni de caída. **No tiene embestida.**

**2) Sus ataques**
- **Láser lento.** Apunta con una pinza (la del lado donde estás), junta energía un ratito (se ve una bolita violeta que crece, suena una campanita cada vez más aguda y marca con puntitos para dónde va) y dispara un rayo que te **sigue girando despacio** (16 grados por segundo; 20 cuando está enojado). Si corrés sin parar dando vueltas alrededor suyo, el rayo no te alcanza: te sigue, pero siempre llega tarde. Si parás o corrés derecho, te agarra. El rayo hace **5 corazones** de daño y como mucho te pega una vez por segundo. Se corta contra los bloques (podés taparte detrás de un pilar).
- **Línea de pinchos.** Golpea el suelo y salen pinchos de cristal en **línea recta hacia donde estás**, uno atrás del otro. Son entidades: salen del piso, lastiman a quien los toque (**3 corazones** y te levantan un poquito) y **desaparecen a los 5 segundos**. La línea queda fija hacia donde estabas cuando golpeó, así que alcanza con moverse de costado.
- **Ataque del centro.** Se va caminando al **centro de la guarida** (donde apareció), junta energía con un anillo en el piso que se cierra y hace **una de 3 variantes** al azar:
  1. **Un láser que gira**: un rayo pegado al piso que da **5 vueltas** completas (6 segundos cada una). Hay que saltarlo.
  2. **Dos láseres que giran**: igual pero con dos rayos, uno de cada lado (opuestos).
  3. **Lluvia de amatistas**: no gira. Tira amatistas al azar en un **área de 12 bloques** alrededor del centro; cada una tarda un poquito en caer (se ve un círculo violeta en el piso donde va a caer) y hace **2 corazones** de daño en un área chica.
- **Golpe normal** cuando estás cerca.
- Con **menos de la mitad de vida** se enoja: espera menos entre ataques, el láser gira un poco más rápido y tira más amatistas.
- **Si le tapan el camino** (por ejemplo cuando va al centro, o cuando te persigue y se traba contra una pared o un pilar) **rompe los bloques que estorban**. Respeta la regla del juego "los mobs rompen bloques" (`mobGriefing`): si la apagás, no rompe nada. No rompe bloques irrompibles.

**3) La guarida geoda.** Es una **geoda gigante aplastada**, más ancha que alta y ovalada: el interior mide **38 bloques de ancho por 32 de fondo** y **13 de alto en el medio** (más baja hacia los costados, como una cúpula), con piso plano. La cáscara tiene 3 capas como una geoda de verdad: **amatista** adentro (con algo de amatista en brote), **calcita** y **basalto liso** afuera. Adentro hay **racimos y brotes de amatista** pegados a las paredes, al techo y al piso, y **pilares de cristal** con obsidiana llorona arriba que dan luz (y sirven de cobertura contra el láser). En el centro está el pedestal con el huevo del jefe (como en las otras guaridas), contra la pared hay **9 nidos de cangrejo de amatista** y hay una puerta de 5 x 6 hacia uno de los cuatro lados (del otro lado hay roca, hay que cavar). Aparece sola bajo tierra (mundo normal, entre las alturas -48 y 32, poco frecuente) y para probarla sin buscarla:
`/cobinatio guarida amatista` (hace falta ser operador; te deja adentro, a un costado).

**4) El huevo.** El jefe, al morir, suelta **12 a 24 bloques de amatista** y el **Huevo de Cangrejo de Amatista con imperfecciones**. Se hace igual que los otros: se pule en la **piedra de afilar** (sale el huevo pulido) y se coloca arriba de un **bloque de amatista** (también sirve el amatista en brote); a los 10 minutos nace un **cangrejo de amatista chico** que toma de dueño al primer jugador que esté a menos de 5 bloques. Los huevos que ponen los jugadores no despiertan al jefe; solo el de la guarida. Las texturas del huevo (bloque e ítems) las armé yo repintando las del huevo de carbón en violeta; si querés dibujarlas vos, son `textures/block/amethyst_crab_egg.png` y `textures/item/amethyst_crab_egg.png` / `amethyst_crab_egg_rough.png`.

**5) Mascotas sentadas.** Todas las mascotas que nacen de los huevos (carbón, cobre y amatista) se pueden sentar para que no te sigan:
- **Clic derecho** a tu cangrejo (con la mano que sea) lo **sienta**; otro clic derecho hace que **te siga otra vez**. Te avisa en pantalla. Se agacha un poquito y abre las patas para que se note.
- La mascota de **carbón con horno** ya usa el clic derecho para abrir el horno, así que a esa se la sienta con **agachado (Shift) + clic derecho**. (Agachado + clic derecho funciona en todas.)
- Solo el dueño puede sentarla. Sentada **no camina ni te sigue** (aunque te vayas lejos o salgas del mundo y vuelvas), pero **sigue funcionando**: el horno sigue cocinando, el pararrayos sigue tirando rayos y dispara a los monstruos que se le acerquen (hasta 14 bloques). Se guarda con el cangrejo.

Todo se puede cambiar en el config: `amethystBossHealth`, `amethystLaserDamage` (10 = 5 corazones), `amethystSpikeDamage` (6 = 3 corazones), `amethystShardDamage` (4 = 2 corazones), `amethystCenterTurns` (cuántas vueltas da el láser del centro, 5).

Cosas que supuse (decime si lo querías distinto)
- "5 de daño" del láser lo entendí como **5 corazones** (10 puntos), igual que "2 corazones" de cada amatista. Si eran 5 puntos (2,5 corazones), poné `amethystLaserDamage = 5.0` en el config.
- "Círculo aplastado / óvalo": la hice un óvalo visto de arriba (38 x 32) **y** aplastada de alto (13), como una geoda gigante apoyada en el piso. Si la querés más redonda o más ancha, se cambia fácil (los números están al principio de la parte de la geoda en `LairFeature.java`).
- El jefe de amatista **no tiene música** (no la pediste); si querés una, se agrega igual que la del cobre.
- Las mascotas se sientan con **clic derecho** (como el perro). Si lo querías con otro botón, se cambia.
- El láser lento y el del centro **pueden dar el golpe una vez por segundo** a cada uno que toquen (no daño continuo), para que 5 corazones no te maten en un tick.

Archivos nuevos: `crab/AmethystCrabBossEntity.java` (el jefe y sus ataques), `AmethystCrabBossModel.java` + `AmethystMeshData.java` (modelo), `AmethystCrabBossRenderer.java` (dibujo del jefe y el rayo), `CrystalSpikeEntity/Renderer` (pinchos), `AmethystShardEntity/Renderer` (amatistas). Cambiados: `LairFeature` (la geoda), `LairCommand`, `ModBlocks` (huevo de amatista y su guarida), `CrabEggBlock`, `RoughEggItem`, `PolishedEggItem`, `EggEvents`, `ModEntities`, `GrottolVariantEntity` + `GrottolVariantModel` + `GuardHandler` (mascotas sentadas), `Config`, `ModRegistry` (pestaña del creativo), `CrabEvents`, `ClientSetup`, textos en los 3 idiomas.

## v1.19.0 - Música del jefe de amatista y la mascota de amatista que hace crecer amatista

**1) Música del jefe.** El Cangrejo Titán de Amatista ahora tiene su canción, **"Sovereign of the Deep"** (la que mandaste). Funciona igual que la del jefe de cobre: empieza cuando el jefe aparece, se escucha **a todo volumen hasta 6 bloques** y va bajando hasta apagarse a los **15 bloques**, y se corta cuando el jefe muere (si la canción termina antes, vuelve a empezar). El archivo es `sounds/amethyst_boss_music.ogg` (si querés cambiarla, reemplazá ese archivo por otro .ogg con el mismo nombre). Para esto el reproductor de música (`BossMusicClient`) quedó general: sirve para cualquier jefe.

**2) Mascota de amatista: "Amatista que crece".** Como no sabías qué hacerle, le inventé esto (la mascota de carbón tiene el horno y la de cobre el pararrayos; esta hace crecer cristales):
- Cada **3 minutos** hace crecer **1 o 2 fragmentos de amatista** y los **suelta a sus pies** (con brillitos y el sonido de cristal de la amatista).
- Unos 3 segundos antes **titila y suena una campanita cada vez más aguda**, para que sepas que está por soltar.
- Si la dejás **sentada**, concentra la energía y tarda **la mitad** (1 minuto y medio). Así sirve de "granja" de amatista: la sentás al lado de tu base y pasás a juntar.
- Solo la mascota (la que nació de un huevo); los cangrejos de amatista salvajes no lo hacen. El tiempo que lleva juntado se guarda con el cangrejo. Para que crezca tiene que estar en una zona cargada (cerca tuyo).
- Se puede cambiar en el config: `amethystPetShardSeconds` (180 = 3 minutos).
- Al nacer, el mensaje y el texto del huevo pulido ahora avisan que "te deja fragmentos de amatista".

Si preferís otra función para esta mascota, se cambia (por ejemplo, un escudo de cristal que te protege cuando te pegan, o que tire amatistas a los monstruos como lo hacen la de carbón y la de cobre). Decime y lo cambio.

Archivos cambiados: `crab/BossMusicClient.java`, `ModSounds.java`, `AmethystCrabBossEntity.java` y `CopperCrabBossEntity.java` (música), `GrottolVariantEntity.java` (mascota), `Config.java`, `sounds.json`, los 3 idiomas, `LEEME.md` y `gradle.properties`. Archivo nuevo: `sounds/amethyst_boss_music.ogg`.

## v1.20.0 - Corazones de los jefes, la Bobina y los discos de música

Esto sale de tu PowerPoint. Lo que pediste ahí lo hago en 3 entregas; esta es la primera. Las gafas de amatista (v1.21.0) y el dicriti con el pico lanzable (v1.22.0) vienen después.

**1) Corazón de cobre y Corazón de amatista.** Los suelta **siempre** el jefe correspondiente al morir, junto con lo que ya soltaba. No hacen nada solos: el de cobre sirve para craftear la Bobina y el de amatista las Gafas de amatista (v1.21.0).

**2) Bobina** (artefacto, va en la ranura de amuletos). Se craftea en la mesa de crafteo con el corazón de cobre en el centro, **4 lingotes de hierro** arriba, abajo, izquierda y derecha, y **4 pepitas de hierro** en las esquinas (igual que en tu dibujo). Mientras la llevás puesta sube el **poder de los hechizos de rayo** de Iron's Spells un **+20 %** (se cambia en el config: `bobinaLightningPower`, 0.20 = 20 %). Nada más.

**3) Discos de música.** Cada jefe con música tiene su disco: **"Colossus Beneath the Crags"** (cobre) y **"Sovereign of the Deep"** (amatista), con tus texturas. Los suelta el jefe con **25 % de probabilidad**. Van en el tocadiscos como cualquier disco. El jefe de carbón todavía no tiene música, así que todavía no tiene disco; cuando le hagamos una canción se agrega igual.

**4) Logros nuevos** (en la pestaña Cobinatio):
- **Ni el rayo pudo contra mí**: derrotar al Cangrejo Titán de Cobre.
- **100 voltios**: craftear la Bobina.
- **Su brillo no pudo contra mí**: derrotar al Cangrejo Titán de Amatista.
Los logros de jefe se los lleva el que le dio el golpe final **y todos los jugadores a menos de 48 bloques** cuando muere.

**5) Más ranuras de amuletos.** Como la Bobina, las Gafas de amatista (que vienen en la próxima versión) y el Doble Almacén van todos en la ranura de amuletos, y por defecto hay **una sola**, ahora hay **3** (si no, no podías llevar dos a la vez).

Notas sobre las texturas: las saqué de las imágenes del PowerPoint. El corazón de cobre y el de amatista son de 10x10 píxeles y la bobina de 12x12, así que se ven con "píxeles más grandes" que el resto; los discos eran de 16x10 y los puse centrados en un cuadrado de 16x16. Si querés que se vean mejor, mandame las imágenes originales (.png) y las reemplazo.

Archivos nuevos: `ModItems.java`, `BobinaItem.java`, `TipItem.java`, `BossRewards.java` (logros), la receta de la bobina, 3 logros, 5 texturas y 5 modelos de ítem, y `data/mutantraisedead/curios/slots/charm.json` (las 3 ranuras). Cambiados: `MutantRaiseDead.java`, `ModRegistry.java` (pestaña), `Config.java`, las tablas de botín de los jefes de cobre y de amatista, `curios/tags/items/charm.json`, los 3 idiomas, `LEEME.md` y `gradle.properties`.
