package com.wordle.wordle.service;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.*;

@Service
public class WordleService {

    // Load all valid 5-letter words from file
    private static final Set<String> VALID_WORDS = new HashSet<>();

    static {
        try {
            InputStream is = WordleService.class.getResourceAsStream("/words.txt");
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    String word = line.trim().toUpperCase();
                    if (word.length() == 5) {
                        VALID_WORDS.add(word);
                    }
                }
                reader.close();
                System.out.println("Loaded " + VALID_WORDS.size() + " valid 5-letter words");
            } else {
                System.err.println("words.txt not found in resources!");
            }
        } catch (Exception e) {
            System.err.println("Failed to load word list: " + e.getMessage());
        }
    }

    private static final List<String> NORMAL_WORDS = List.of(
            "CRANE", "SLATE", "TRAIN", "LIGHT", "STONE",
            "PLUMB", "GRIEF", "STAMP", "TOWER", "BLAZE",
            "CLOTH", "DRAFT", "FRAME", "GLINT", "HOVER",
            "BRAVE", "CHEST", "FIELD", "GLOBE", "HINGE",
            "CRUDE","CURVE","DEPTH","DENSE","DEBUT","DELTA","DRANK","DRAWN","DYING","EAGER",
            "ELITE","EMPTY","ENEMY","EPOCH","EXACT","EXIST","EXTRA","FAULT","FIBER","FLEET",
            "FLUID","FORTH","FRAUD","FRESH","GIANT","GLASS","GRAVE","GROSS","GUARD","HENCE",
            "INDEX","INNER","ISSUE","JOINT","JUDGE","LASER","LAYER","LEMON","LIMIT","MAGIC",
            "METAL","MINOR","MINUS","MODEL","MORAL","MOTOR","NOVEL","OCCUR","PHASE","PRIOR"
    );

    private static final List<String> TIME_WORDS = List.of(
            "ADIEU", "STARE", "BREAK", "PRIZE", "WORLD",
            "DRINK", "FLOAT", "GRAZE", "JOUST", "KNEEL",
            "LAPSE", "MANOR", "NERVE", "ONSET", "REALM",
            "SPARK", "THINK", "ULTRA", "VAULT", "WRATH",
            "ARROW","ARRAY","BOOST","BLEED","BLESS","BLOOM","BLOOD","CIVIC","LEVEL","LOCAL",
            "LOOSE","SHELF","SHELL","SHIFT","SHOCK","SHOOT","SIXTH","SIZED","SMOKE","SOLID",
            "STICK","STOOD","SWING","THICK","THIRD","THREW","THROW","TIGHT","TRICK","TRIED",
            "TRUCK","TRUNK","TRUST","TWICE","UNDUE","UNIFY","UPSET","URBAN","USUAL","VALID",
            "VALUE","VIRUS","VOCAL","WASTE","WATCH","WHEEL","WHOSE","WORTH","WRITE","YOUTH"
    );

    private static final List<String> SUDDEN_WORDS = List.of(
            "HELLO", "BRAVE", "FROST", "GLOOM", "HARSH",
            "INPUT", "JOKER", "KNACK", "LIVER", "MIXER",
            "NOBLE", "ORBIT", "PATSY", "QUIRK", "REACH",
            "SALVO", "TIMID", "UNDER", "VENOM", "WALTZ",
            "ACUTE","ADMIT","ADOPT","AGENT","ALIGN","AMBER","AMEND","ANGEL","ANGLE","APART",
            "ARGUE","ARRAY","ASSET","AUDIO","AVOID","BASIN","BENCH","BLAME","BLAST","BLEED",
            "BLESS","BOOST","BOUND","BRASS","BRIEF","BROAD","CHAIN","CHAOS","CHARM","CHART",
            "CHASE","CHEST","CIVIL","CLAIM","CRAFT","CRASH","CRIME","CRUDE","CURVE","CYCLE",
            "DEBUT","DELAY","DELTA","DENSE","DEPTH","DRAFT","DRILL","ELITE","ENTRY","EXACT"
    );

    private static final Map<String, String> WORD_HINTS = new HashMap<>();

    static {
        WORD_HINTS.put("CRANE", "💡 A tall machine used on construction sites");
        WORD_HINTS.put("SLATE", "💡 A grey rock used for roofs and chalkboards");
        WORD_HINTS.put("TRAIN", "💡 Travels on tracks and carries passengers");
        WORD_HINTS.put("LIGHT", "💡 What you need to see in the dark");
        WORD_HINTS.put("STONE", "💡 A hard, solid piece of rock");
        WORD_HINTS.put("PLUMB", "💡 A weight on a string to check if something is vertical");
        WORD_HINTS.put("GRIEF", "💡 Deep sadness, especially after a loss");
        WORD_HINTS.put("STAMP", "💡 You put this on a letter before mailing it");
        WORD_HINTS.put("TOWER", "💡 A very tall, narrow building");
        WORD_HINTS.put("BLAZE", "💡 A large, fierce fire");
        WORD_HINTS.put("CLOTH", "💡 Fabric used to make clothes or wipe surfaces");
        WORD_HINTS.put("DRAFT", "💡 The first version of a written document");
        WORD_HINTS.put("FRAME", "💡 What you put around a picture");
        WORD_HINTS.put("GLINT", "💡 A quick flash or sparkle of light");
        WORD_HINTS.put("HOVER", "💡 To float in the air without moving");
        WORD_HINTS.put("BRAVE", "💡 Having courage, not afraid of danger");
        WORD_HINTS.put("CHEST", "💡 The front part of your body between neck and stomach");
        WORD_HINTS.put("FIELD", "💡 An open area of land, often used for farming");
        WORD_HINTS.put("GLOBE", "💡 A round model of the Earth");
        WORD_HINTS.put("HINGE", "💡 The metal joint that lets a door swing open");
        WORD_HINTS.put("CRUDE", "💡 In a natural or raw state; not refined");
        WORD_HINTS.put("CURVE", "💡 A line that bends smoothly");
        WORD_HINTS.put("DEPTH", "💡 The distance from top to bottom");
        WORD_HINTS.put("DENSE", "💡 Closely packed together");
        WORD_HINTS.put("DEBUT", "💡 A first public appearance");
        WORD_HINTS.put("DELTA", "💡 Land formed at the mouth of a river");
        WORD_HINTS.put("DRANK", "💡 Past tense of drink");
        WORD_HINTS.put("DRAWN", "💡 Past participle of draw");
        WORD_HINTS.put("DYING", "💡 Nearing death or fading away");
        WORD_HINTS.put("EAGER", "💡 Very excited or enthusiastic");
        WORD_HINTS.put("ELITE", "💡 The most powerful or skilled group");
        WORD_HINTS.put("EMPTY", "💡 Containing nothing");
        WORD_HINTS.put("ENEMY", "💡 A person who is against you");
        WORD_HINTS.put("EPOCH", "💡 A long period of time in history");
        WORD_HINTS.put("EXACT", "💡 Completely correct or precise");
        WORD_HINTS.put("EXIST", "💡 To be real or alive");
        WORD_HINTS.put("EXTRA", "💡 More than what is needed");
        WORD_HINTS.put("FAULT", "💡 A mistake or weakness");
        WORD_HINTS.put("FIBER", "💡 Thread-like material in plants or fabric");
        WORD_HINTS.put("FLEET", "💡 A group of ships or vehicles");
        WORD_HINTS.put("FLUID", "💡 A substance that flows like water");
        WORD_HINTS.put("FORTH", "💡 Forward in time or place");
        WORD_HINTS.put("FRAUD", "💡 Cheating for financial gain");
        WORD_HINTS.put("FRESH", "💡 Recently made or obtained");
        WORD_HINTS.put("GIANT", "💡 Extremely large person or thing");
        WORD_HINTS.put("GLASS", "💡 Transparent material used for windows");
        WORD_HINTS.put("GRAVE", "💡 A place where someone is buried");
        WORD_HINTS.put("GROSS", "💡 Total amount before deductions");
        WORD_HINTS.put("GUARD", "💡 A person who protects");
        WORD_HINTS.put("HENCE", "💡 For this reason");
        WORD_HINTS.put("INDEX", "💡 A list of topics in a book");
        WORD_HINTS.put("INNER", "💡 Located inside");
        WORD_HINTS.put("ISSUE", "💡 An important topic or problem");
        WORD_HINTS.put("JOINT", "💡 A place where two parts connect");
        WORD_HINTS.put("JUDGE", "💡 A person who decides in court");
        WORD_HINTS.put("LASER", "💡 A focused beam of light");
        WORD_HINTS.put("LAYER", "💡 One level placed over another");
        WORD_HINTS.put("LEMON", "💡 A sour yellow fruit");
        WORD_HINTS.put("LIMIT", "💡 The maximum allowed amount");
        WORD_HINTS.put("MAGIC", "💡 Supernatural power or illusion");
        WORD_HINTS.put("METAL", "💡 A hard, shiny material like iron");
        WORD_HINTS.put("MINOR", "💡 Less important or smaller");
        WORD_HINTS.put("MINUS", "💡 Indicates subtraction");
        WORD_HINTS.put("MODEL", "💡 A representation or example");
        WORD_HINTS.put("MORAL", "💡 A lesson about right and wrong");
        WORD_HINTS.put("MOTOR", "💡 A machine that produces motion");
        WORD_HINTS.put("NOVEL", "💡 A long fictional story");
        WORD_HINTS.put("OCCUR", "💡 To happen");
        WORD_HINTS.put("PHASE", "💡 A stage in a process");
        WORD_HINTS.put("PRIOR", "💡 Existing before something else");

        WORD_HINTS.put("ADIEU", "💡 French word for 'goodbye'");
        WORD_HINTS.put("STARE", "💡 To look at something for a long time with wide eyes");
        WORD_HINTS.put("BREAK", "💡 To separate into pieces or take a rest");
        WORD_HINTS.put("PRIZE", "💡 What you win in a competition");
        WORD_HINTS.put("WORLD", "💡 The Earth and all the people on it");
        WORD_HINTS.put("DRINK", "💡 To swallow liquid");
        WORD_HINTS.put("FLOAT", "💡 To stay on the surface of water without sinking");
        WORD_HINTS.put("GRAZE", "💡 What cows do when they eat grass in a field");
        WORD_HINTS.put("JOUST", "💡 Medieval knights fighting on horseback with lances");
        WORD_HINTS.put("KNEEL", "💡 To go down on your knees");
        WORD_HINTS.put("LAPSE", "💡 A temporary failure or slip in memory");
        WORD_HINTS.put("MANOR", "💡 A large house in the countryside with land");
        WORD_HINTS.put("NERVE", "💡 Carries signals between brain and body");
        WORD_HINTS.put("ONSET", "💡 The beginning or start of something");
        WORD_HINTS.put("REALM", "💡 A kingdom or field of activity");
        WORD_HINTS.put("SPARK", "💡 A tiny, bright particle from a fire");
        WORD_HINTS.put("THINK", "💡 To use your mind to consider or reason");
        WORD_HINTS.put("ULTRA", "💡 Going beyond what is normal; extreme");
        WORD_HINTS.put("VAULT", "💡 A secure room for storing valuables");
        WORD_HINTS.put("WRATH", "💡 Extreme anger or fury");
        WORD_HINTS.put("ARROW", "💡 A pointed projectile shot from a bow");
        WORD_HINTS.put("ARRAY", "💡 An organized arrangement of items");
        WORD_HINTS.put("BOOST", "💡 To increase or improve something");
        WORD_HINTS.put("BLEED", "💡 To lose blood from a wound");
        WORD_HINTS.put("BLESS", "💡 To give divine approval or protection");
        WORD_HINTS.put("BLOOM", "💡 A flower in full growth");
        WORD_HINTS.put("BLOOD", "💡 The red liquid that flows in veins");
        WORD_HINTS.put("CIVIC", "💡 Related to a city or community");
        WORD_HINTS.put("LEVEL", "💡 Completely flat or even");
        WORD_HINTS.put("LOCAL", "💡 Belonging to a specific area");
        WORD_HINTS.put("LOOSE", "💡 Not tight or firmly fixed");
        WORD_HINTS.put("SHELF", "💡 A flat board used for storage");
        WORD_HINTS.put("SHELL", "💡 The hard outer covering of something");
        WORD_HINTS.put("SHIFT", "💡 To move or change position");
        WORD_HINTS.put("SHOCK", "💡 A sudden upsetting event");
        WORD_HINTS.put("SHOOT", "💡 To fire a bullet or take a photo");
        WORD_HINTS.put("SIXTH", "💡 Coming after fifth");
        WORD_HINTS.put("SIZED", "💡 Having a particular measurement");
        WORD_HINTS.put("SMOKE", "💡 The gray gas from fire");
        WORD_HINTS.put("SOLID", "💡 Firm and not liquid or gas");
        WORD_HINTS.put("STICK", "💡 A thin piece of wood");
        WORD_HINTS.put("STOOD", "💡 Past tense of stand");
        WORD_HINTS.put("SWING", "💡 To move back and forth");
        WORD_HINTS.put("THICK", "💡 Having a large distance between sides");
        WORD_HINTS.put("THIRD", "💡 Coming after second");
        WORD_HINTS.put("THREW", "💡 Past tense of throw");
        WORD_HINTS.put("THROW", "💡 To toss something through the air");
        WORD_HINTS.put("TIGHT", "💡 Firmly fixed in place");
        WORD_HINTS.put("TRICK", "💡 A clever act meant to deceive");
        WORD_HINTS.put("TRIED", "💡 Past tense of try");
        WORD_HINTS.put("TRUCK", "💡 A large vehicle for carrying goods");
        WORD_HINTS.put("TRUNK", "💡 The main stem of a tree");
        WORD_HINTS.put("TRUST", "💡 Firm belief in someone");
        WORD_HINTS.put("TWICE", "💡 Two times");
        WORD_HINTS.put("UNDUE", "💡 More than necessary or appropriate");
        WORD_HINTS.put("UNIFY", "💡 To bring together as one");
        WORD_HINTS.put("UPSET", "💡 Unhappy or emotionally disturbed");
        WORD_HINTS.put("URBAN", "💡 Related to a city");
        WORD_HINTS.put("USUAL", "💡 Common or normal");
        WORD_HINTS.put("VALID", "💡 Logically correct or acceptable");
        WORD_HINTS.put("VALUE", "💡 The worth of something");
        WORD_HINTS.put("VIRUS", "💡 A tiny infectious agent");
        WORD_HINTS.put("VOCAL", "💡 Related to the voice");
        WORD_HINTS.put("WASTE", "💡 To use carelessly or improperly");
        WORD_HINTS.put("WATCH", "💡 A device worn to tell time");
        WORD_HINTS.put("WHEEL", "💡 A circular object that rolls");
        WORD_HINTS.put("WHOSE", "💡 Belonging to which person");
        WORD_HINTS.put("WORTH", "💡 The value of something");
        WORD_HINTS.put("WRITE", "💡 To form letters or words");
        WORD_HINTS.put("YOUTH", "💡 The period of being young");

        WORD_HINTS.put("HELLO", "💡 A common greeting when you meet someone");
        WORD_HINTS.put("FROST", "💡 Ice crystals that form on cold surfaces");
        WORD_HINTS.put("GLOOM", "💡 Darkness or a feeling of sadness");
        WORD_HINTS.put("HARSH", "💡 Rough, unpleasant, or severe");
        WORD_HINTS.put("INPUT", "💡 Information or data entered into a system");
        WORD_HINTS.put("JOKER", "💡 Someone who tells jokes or a wild card");
        WORD_HINTS.put("KNACK", "💡 A special skill or talent for something");
        WORD_HINTS.put("LIVER", "💡 An organ that filters your blood");
        WORD_HINTS.put("MIXER", "💡 A machine that blends ingredients together");
        WORD_HINTS.put("NOBLE", "💡 Having high moral qualities or aristocratic rank");
        WORD_HINTS.put("ORBIT", "💡 The path a planet takes around a star");
        WORD_HINTS.put("PATSY", "💡 Someone who is easily blamed or taken advantage of");
        WORD_HINTS.put("QUIRK", "💡 A strange habit or unusual behavior");
        WORD_HINTS.put("REACH", "💡 To stretch out to touch or grab something");
        WORD_HINTS.put("SALVO", "💡 A simultaneous firing of guns");
        WORD_HINTS.put("TIMID", "💡 Shy and lacking confidence");
        WORD_HINTS.put("UNDER", "💡 Below or beneath something");
        WORD_HINTS.put("VENOM", "💡 Poison produced by snakes or spiders");
        WORD_HINTS.put("WALTZ", "💡 A ballroom dance in 3/4 time");
        WORD_HINTS.put("ACUTE", "💡 Sharp or severe in effect");
        WORD_HINTS.put("ADMIT", "💡 To confess or allow entry");
        WORD_HINTS.put("ADOPT", "💡 To take legally as one's own");
        WORD_HINTS.put("AGENT", "💡 A person who acts on behalf of another");
        WORD_HINTS.put("ALIGN", "💡 To arrange in a straight line");
        WORD_HINTS.put("AMBER", "💡 A yellow-orange fossilized resin");
        WORD_HINTS.put("AMEND", "💡 To make changes for improvement");
        WORD_HINTS.put("ANGEL", "💡 A spiritual heavenly being");
        WORD_HINTS.put("ANGLE", "💡 The space between two intersecting lines");
        WORD_HINTS.put("APART", "💡 Separated by distance");
        WORD_HINTS.put("ARGUE", "💡 To present reasons for or against");
        WORD_HINTS.put("ARRAY", "💡 An ordered arrangement of items");
        WORD_HINTS.put("ASSET", "💡 Something valuable or useful");
        WORD_HINTS.put("AUDIO", "💡 Related to sound");
        WORD_HINTS.put("AVOID", "💡 To stay away from");
        WORD_HINTS.put("BASIN", "💡 A wide open container or bowl");
        WORD_HINTS.put("BENCH", "💡 A long seat for multiple people");
        WORD_HINTS.put("BLAME", "💡 To hold responsible for a fault");
        WORD_HINTS.put("BLAST", "💡 A strong explosion or burst");
        WORD_HINTS.put("BLEED", "💡 To lose blood from injury");
        WORD_HINTS.put("BLESS", "💡 To give protection or approval");
        WORD_HINTS.put("BOOST", "💡 To increase or improve");
        WORD_HINTS.put("BOUND", "💡 Destined or tied to something");
        WORD_HINTS.put("BRASS", "💡 A yellow alloy of copper and zinc");
        WORD_HINTS.put("BRIEF", "💡 Short in duration");
        WORD_HINTS.put("BROAD", "💡 Wide in extent");
        WORD_HINTS.put("CHAIN", "💡 Connected series of links");
        WORD_HINTS.put("CHAOS", "💡 Complete disorder or confusion");
        WORD_HINTS.put("CHARM", "💡 The power to attract or delight");
        WORD_HINTS.put("CHART", "💡 A graphical representation of data");
        WORD_HINTS.put("CHASE", "💡 To run after something");
        WORD_HINTS.put("CHEST", "💡 The front part of the human torso");
        WORD_HINTS.put("CIVIL", "💡 Related to citizens or polite behavior");
        WORD_HINTS.put("CLAIM", "💡 To state something as true");
        WORD_HINTS.put("CRAFT", "💡 Skill in making things by hand");
        WORD_HINTS.put("CRASH", "💡 A sudden collision or breakdown");
        WORD_HINTS.put("CRIME", "💡 An illegal act");
        WORD_HINTS.put("CRUDE", "💡 In a raw or unrefined state");
        WORD_HINTS.put("CURVE", "💡 A line that bends smoothly");
        WORD_HINTS.put("CYCLE", "💡 A repeating series of events");
        WORD_HINTS.put("DEBUT", "💡 A first public appearance");
        WORD_HINTS.put("DELAY", "💡 To postpone or slow down");
        WORD_HINTS.put("DELTA", "💡 Land formed at a river's mouth");
        WORD_HINTS.put("DENSE", "💡 Closely compacted together");
        WORD_HINTS.put("DEPTH", "💡 Distance from top to bottom");
        WORD_HINTS.put("DRAFT", "💡 A preliminary version of something");
        WORD_HINTS.put("DRILL", "💡 A tool used to make holes");
        WORD_HINTS.put("ELITE", "💡 A select group of superior quality");
        WORD_HINTS.put("ENTRY", "💡 The act of going in");
        WORD_HINTS.put("EXACT", "💡 Completely accurate or precise");
    }

    private int getDailyIndex(int listSize) {
        LocalDate today = LocalDate.now();
        int hash = today.getYear() * 10000
                + today.getMonthValue() * 100
                + today.getDayOfMonth();
        return Math.abs(hash) % listSize;
    }

    public String getWordForStandardMode(String mode) {
        if (mode == null) mode = "normal";
        switch (mode.toLowerCase()) {
            case "time":
                return TIME_WORDS.get(getDailyIndex(TIME_WORDS.size()));
            case "sudden":
                return SUDDEN_WORDS.get(getDailyIndex(SUDDEN_WORDS.size()));
            default:
                return NORMAL_WORDS.get(getDailyIndex(NORMAL_WORDS.size()));
        }
    }

    public boolean isValidWord(String word) {
        if (word == null || word.length() != 5) return false;
        return VALID_WORDS.contains(word.toUpperCase());
    }

    public String getHintForMode(String mode) {
        String word = getWordForStandardMode(mode);
        return WORD_HINTS.getOrDefault(word, "💡 Guess the 5-letter word!");
    }

    public String evaluate(String guess, String word) {
        char[] result = new char[5];
        boolean[] used = new boolean[5];

        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == word.charAt(i)) {
                result[i] = 'G';
                used[i] = true;
            }
        }

        for (int i = 0; i < 5; i++) {
            if (result[i] == 'G') continue;
            boolean found = false;
            for (int j = 0; j < 5; j++) {
                if (!used[j] && guess.charAt(i) == word.charAt(j)) {
                    found = true;
                    used[j] = true;
                    break;
                }
            }
            result[i] = found ? 'Y' : 'X';
        }

        return new String(result);
    }
}