# Combined outcomes

## Groups

Groups are simply a combination of multiple outcomes, all of which take place at the same time. Each outcome is separated by a semicolon `;`, e.g.

```
group(type=item,ID=diamond;type=entity,ID=pig)
```

You can also specify the number of outcomes that should be selected. Each outcome has an equal chance, though the same one will never be selected twice.

```
group:2:(type=item,ID=iron_sword;type=item,ID=iron_pickaxe,type=item,ID=iron_axe)
```

Groups can also be nested within groups:

```
group(ID=wooden_sword;group:1:(type=entity,ID=creeper;type=entity,ID=zombie))
```

## Luck level

-   Property: `@luck`

Luck level is and additional property which describes how 'lucky' an outcome is, in the range `-2..2`. When the luck property of the source block/item/etc is increased/decreased, outcomes with a higher/lower luck level become more likely. The default luck level is `0`. Below is a rough guide for all of the levels:

-   `-2`: Very unlucky. Might kill the player or do great damage.
-   `-1`: Unlucky. Does something that the player would not want, or something completely useless.
-   `0`: Neutral. Not bad, not useless, but fairly average.
-   `1`: Lucky. Something that the player would be happy to receive.
-   `2`: Very lucky. The best thing the player could hope for.

```
ID=diamond,amount=4@luck=1
```

## Chance

-   Property: `@chance`

Chance is an additional property which describes how likely the outcome is to be chosen. By default, all outcomes are equally likely. Using `@chance=0.5` makes an outcome half as likely, while `@chance=4` makes it 4 times as likely.

```
type=entity,ID=Wither@luck=-2@chance=0.1
```
