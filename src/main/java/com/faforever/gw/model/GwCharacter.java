package com.faforever.gw.model;

import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@Setter
@Entity
@Table(name = "gw_character")
public class GwCharacter implements Serializable {
    private UUID id;
    private int fafId;
    private String name;
    private Faction faction;
    private Long xp;
    private List<BattleParticipant> battleParticipantList = new ArrayList<>();
    private GwCharacter killer;
    private Set<GwCharacter> killedBy;
    private Rank rank;

    @Id
//    @GeneratedValue(generator = "uuid2")
//    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    public UUID getId() {
        return id;
    }

    @Column(name = "faf_id", nullable = false, updatable = false)
    public int getFafId() {
        return fafId;
    }

    @Column(name = "name", nullable = false, updatable = false, length = 30)
    public String getName() {
        return name;
    }

    @Column(name = "faction", nullable = false, updatable = false, length = 1)
    public Faction getFaction() {
        return faction;
    }

    @Column(name = "xp", nullable = false, updatable = false)
    public Long getXp() {
        return xp;
    }

    @OneToMany(mappedBy = "character")
    public List<BattleParticipant> getBattleParticipantList() {
        return battleParticipantList;
    }

    @ManyToOne
    @JoinColumn(name = "fk_killer")
    public GwCharacter getKiller() {
        return killer;
    }

    @OneToMany(mappedBy = "killer")
    public Set<GwCharacter> getKilledBy() {
        return killedBy;
    }

    @ManyToOne
    @JoinColumn(name = "fk_rank")
    public Rank getRank() {
        return rank;
    }

    @Transient
    public String getTitle() {
        return rank.getTitle(getFaction());
    }
}
