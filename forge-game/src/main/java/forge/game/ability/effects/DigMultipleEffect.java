package forge.game.ability.effects;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;

import forge.game.Game;
import forge.game.ability.AbilityUtils;
import forge.game.ability.SpellAbilityEffect;
import forge.game.card.Card;
import forge.game.card.CardCollection;
import forge.game.card.CardLists;
import forge.game.card.CardZoneTable;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.zone.PlayerZone;
import forge.game.zone.ZoneType;

public class DigMultipleEffect extends SpellAbilityEffect {

    @Override
    public void resolve(SpellAbility sa) {
        // TODO Auto-generated method stub
        final Card host = sa.getHostCard();
        final Player player = sa.getActivatingPlayer();
        final Game game = player.getGame();
        Player chooser = player;
        int numToDig = AbilityUtils.calculateAmount(host, sa.getParam("DigNum"), sa);

        final ZoneType srcZone = sa.hasParam("SourceZone") ? ZoneType.smartValueOf(sa.getParam("SourceZone")) : ZoneType.Library;

        final ZoneType destZone1 = sa.hasParam("DestinationZone") ? ZoneType.smartValueOf(sa.getParam("DestinationZone")) : ZoneType.Hand;
        final ZoneType destZone2 = sa.hasParam("DestinationZone2") ? ZoneType.smartValueOf(sa.getParam("DestinationZone2")) : ZoneType.Library;
        int libraryPosition = sa.hasParam("LibraryPosition") ? Integer.parseInt(sa.getParam("LibraryPosition")) : -1;
        final int libraryPosition2 = sa.hasParam("LibraryPosition2") ? Integer.parseInt(sa.getParam("LibraryPosition2")) : -1;

        String changeValid = sa.hasParam("ChangeValid") ? sa.getParam("ChangeValid") : "";

        CardZoneTable table = new CardZoneTable();
        for (final Player p : getTargetPlayers(sa)) {
            if (sa.usesTargeting() && !p.canBeTargetedBy(sa)) {
                continue;
            }
            final CardCollection top = new CardCollection();
            final CardCollection rest = new CardCollection();
            final PlayerZone sourceZone = p.getZone(srcZone);

            numToDig = Math.min(numToDig, sourceZone.size());
            for (int i = 0; i < numToDig; i++) {
                top.add(sourceZone.get(i));
            }

            if (top.isEmpty()) {
                continue;
            }

            rest.addAll(top);

            if (sa.hasParam("Reveal")) {
                game.getAction().reveal(top, p, false);
            } else {
                // reveal cards first
                game.getAction().revealTo(top, player);
            }

            Map<String, CardCollection> validMap = Maps.newHashMap();

            for (final String valid : changeValid.split(",")) {
                CardCollection list = CardLists.getValidCards(top, valid, host.getController(), host, sa);
                if (!list.isEmpty()) {
                    validMap.put(valid, list);
                }
            }

            if (validMap.isEmpty()) {
                chooser.getController().notifyOfValue(sa, null, "No valid cards");
                continue;
            }

            CardCollection chosen = chooser.getController().chooseCardsForEffectMultiple(validMap, sa, "Choose cards");

            if (!chosen.isEmpty()) {
                game.getAction().reveal(chosen, chooser, true,
                    chooser + " picked " + (chosen.size() == 1 ? "this card" : "these cards") + " from ");
            }

            for (Card c : chosen) {
                final ZoneType origin = c.getZone().getZoneType();
                final PlayerZone zone = c.getOwner().getZone(destZone1);

                if (zone.is(ZoneType.Library) || zone.is(ZoneType.PlanarDeck) || zone.is(ZoneType.SchemeDeck)) {
                    if (libraryPosition == -1 || libraryPosition > zone.size()) {
                        libraryPosition = zone.size();
                    }
                    c = game.getAction().moveTo(zone, c, libraryPosition, sa);
                }
                else {
                    c = game.getAction().moveTo(zone, c, sa);
                    if (destZone1.equals(ZoneType.Battlefield)) {
                        if (sa.hasParam("Tapped")) {
                            c.setTapped(true);
                        }
                    }
                }
                if (!origin.equals(c.getZone().getZoneType())) {
                    table.put(origin, c.getZone().getZoneType(), c);
                }

                if (sa.hasParam("ExileFaceDown")) {
                    c.turnFaceDown(true);
                }
                if (sa.hasParam("Imprint")) {
                    host.addImprintedCard(c);
                }
                if (sa.hasParam("ForgetOtherRemembered")) {
                    host.clearRemembered();
                }
                if (sa.hasParam("RememberChanged")) {
                    host.addRemembered(c);
                }
                rest.remove(c);
            }

            // now, move the rest to destZone2
            if (destZone2 == ZoneType.Library || destZone2 == ZoneType.PlanarDeck || destZone2 == ZoneType.SchemeDeck
                    || destZone2 == ZoneType.Graveyard) {
                CardCollection afterOrder = rest;
                if (sa.hasParam("RestRandomOrder")) {
                    CardLists.shuffle(afterOrder);
                }
                if (libraryPosition2 != -1) {
                    // Closest to top
                    Collections.reverse(afterOrder);
                }
                for (final Card c : afterOrder) {
                    final ZoneType origin = c.getZone().getZoneType();
                    Card m;
                    if (destZone2 == ZoneType.Library) {
                        m = game.getAction().moveToLibrary(c, libraryPosition2, sa);
                    }
                    else {
                        m = game.getAction().moveToVariantDeck(c, destZone2, libraryPosition2, sa);
                    }
                    if (m != null && !origin.equals(m.getZone().getZoneType())) {
                        table.put(origin, m.getZone().getZoneType(), m);
                    }
                }
            }
            else {
                // just move them randomly
                for (int i = 0; i < rest.size(); i++) {
                    Card c = rest.get(i);
                    final ZoneType origin = c.getZone().getZoneType();
                    final PlayerZone toZone = c.getOwner().getZone(destZone2);
                    c = game.getAction().moveTo(toZone, c, sa);
                    if (!origin.equals(c.getZone().getZoneType())) {
                        table.put(origin, c.getZone().getZoneType(), c);
                    }
                }
            }
        }
        //table trigger there
        table.triggerChangesZoneAll(game);
    }

}
