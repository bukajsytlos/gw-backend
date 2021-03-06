package com.faforever.gw;

import com.faforever.gw.model.*;
import com.faforever.gw.model.repository.*;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Component
public class DemoDataInitializer {
    private final CharacterRepository characterRepository;
    private final PlanetRepository planetRepository;
    private final MapRepository mapRepository;
    private final BattleRepository battleRepository;
    private final RankRepository rankRepository;

    @Inject
    public DemoDataInitializer(CharacterRepository characterRepository, PlanetRepository planetRepository, MapRepository mapRepository, BattleRepository battleRepository, RankRepository rankRepository) {
        this.characterRepository = characterRepository;
        this.planetRepository = planetRepository;
        this.mapRepository = mapRepository;
        this.battleRepository = battleRepository;
        this.rankRepository = rankRepository;
    }

    @Transactional
    public void run() throws SQLException {
        generateUserToken();

        Rank rank1 = new Rank();
        rank1.setLevel(1);
        rank1.setXpMin(0L);
        rank1.setUefTitle("UNoob");
        rank1.setCybranTitle("CNoob");
        rank1.setAeonTitle("ANoob");
        rank1.setSeraphimTitle("SNoob");
        rankRepository.save(rank1);

        Rank rank2 = new Rank();
        rank2.setLevel(2);
        rank2.setXpMin(1000L);
        rank2.setUefTitle("UExperienced");
        rank2.setCybranTitle("CExperienced");
        rank2.setAeonTitle("AExperienced");
        rank2.setSeraphimTitle("SExperienced");
        rankRepository.save(rank2);

        Rank rank3 = new Rank();
        rank3.setLevel(3);
        rank3.setXpMin(10000L);
        rank3.setUefTitle("UPro");
        rank3.setCybranTitle("CPro");
        rank3.setAeonTitle("APro");
        rank3.setSeraphimTitle("SPro");
        rankRepository.save(rank3);

        GwCharacter character = new GwCharacter();
        character.setId(UUID.fromString("a1111111-e35c-11e6-bf01-fe55135034f3"));
        character.setFafId(1);
        character.setName("UEF Alpha");
        character.setFaction(Faction.UEF);
        character.setXp(999L);
        character.setRank(rank1);
        characterRepository.save(character);

        character = new GwCharacter();
        character.setId(UUID.fromString("a2222222-e35c-11e6-bf01-fe55135034f3"));
        character.setFafId(2);
        character.setName("UEF Bravo");
        character.setFaction(Faction.UEF);
        character.setXp(25000L);
        character.setRank(rank3);
        characterRepository.save(character);

        character = new GwCharacter();
        character.setId(UUID.fromString("a3333333-e4e2-11e6-bf01-fe55135034f3"));
        character.setFafId(3);
        character.setName("Cybran Charlie");
        character.setFaction(Faction.CYBRAN);
        character.setXp(0L);
        character.setRank(rank1);
        characterRepository.save(character);

        character = new GwCharacter();
        character.setId(UUID.fromString("a4444444-e4e2-11e6-bf01-fe55135034f3"));
        character.setFafId(4);
        character.setName("Cybran Delta");
        character.setFaction(Faction.CYBRAN);
        character.setXp(1000L);
        character.setRank(rank2);
        characterRepository.save(character);

        character = new GwCharacter();
        character.setId(UUID.fromString("a5555555-e4e2-11e6-bf01-fe55135034f3"));
        character.setFafId(5);
        character.setName("Aeon Echo");
        character.setFaction(Faction.AEON);
        character.setXp(900L);
        character.setRank(rank1);
        characterRepository.save(character);

        Map map = new Map();
        map.setGround(Ground.SOIL);
        map.setFafMapId(1);
        map.setFafMapVersion(1);
        map.setSize(10);
        map.setTotalSlots(4);
        mapRepository.save(map);

        Planet planet = new Planet();
        planet.setId(UUID.fromString("e1e4c4c4-e35c-11e6-bf01-fe55135034f3"));
        planet.setGround(Ground.SOIL);
        planet.setHabitable(true);
        planet.setOrbitLevel(5);
        planet.setSize(20);

        planet.setMap(map);
        planetRepository.save(planet);

        Battle initBattle = new Battle();
        initBattle.setId(UUID.randomUUID());
        initBattle.setPlanet(planet);
        initBattle.setInitiatedAt(Timestamp.from(Instant.EPOCH));
        initBattle.setStartedAt(Timestamp.from(Instant.EPOCH));
        initBattle.setEndedAt(Timestamp.from(Instant.EPOCH));
        initBattle.setWinningFaction(Faction.CYBRAN);
        initBattle.setStatus(BattleStatus.FINISHED);

        battleRepository.save(initBattle);
    }

    private void generateUserToken() {
        MacSigner macSigner = new MacSigner("secret");
        // {"expires":4102358400, "authorities": [], "user_id": 1, "user_name": "UEF Alpha"}
        // {"expires":4102358400, "authorities": [], "user_id": 2, "user_name": "UEF Bravo"}
        // {"expires":4102358400, "authorities": [], "user_id": 3, "user_name": "Cybran Charlie"}
        // {"expires":4102358400, "authorities": [], "user_id": 4, "user_name": "Cybran Delta"}
        // {"expires":4102358400, "authorities": [], "user_id": 5, "user_name": "Aeon Echo"}

        System.out.println("-1- UEF Alpha");
        Jwt token = JwtHelper.encode("{\"expires\":4102358400, \"authorities\": [], \"user_id\": 1, \"user_name\": \"UEF Alpha\"}", macSigner);
        System.out.println(token.getEncoded());

        System.out.println("-2- UEF Bravo");
        token = JwtHelper.encode("{\"expires\":4102358400, \"authorities\": [], \"user_id\": 2, \"user_name\": \"UEF Bravo\"}", macSigner);
        System.out.println(token.getEncoded());

        System.out.println("-3- Cybran Charlie");
        token = JwtHelper.encode("{\"expires\":4102358400, \"authorities\": [], \"user_id\": 3, \"user_name\": \"Cybran Charlie\"}", macSigner);
        System.out.println(token.getEncoded());

        System.out.println("-4- Cybran Delta");
        token = JwtHelper.encode("{\"expires\":4102358400, \"authorities\": [], \"user_id\": 4, \"user_name\": \"Cybran Delta\"}", macSigner);
        System.out.println(token.getEncoded());

        System.out.println("-5- Aeon Echo");
        token = JwtHelper.encode("{\"expires\":4102358400, \"authorities\": [], \"user_id\": 5, \"user_name\": \"Aeon Echo\"}", macSigner);
        System.out.println(token.getEncoded());
    }
}
