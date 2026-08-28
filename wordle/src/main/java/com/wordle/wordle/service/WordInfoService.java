package com.wordle.wordle.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WordInfoService {

    private static final Map<String, String> DEFINITIONS = new HashMap<>();
    private static final Map<String, String> TRIVIA = new HashMap<>();
    private static final Map<String, String> EXAMPLES = new HashMap<>();

    static {
        DEFINITIONS.put("HELLO", "A greeting or expression of goodwill used when meeting someone.");
        DEFINITIONS.put("CRANE", "A large long-necked bird, or a machine for lifting heavy objects.");
        DEFINITIONS.put("SLATE", "A fine-grained grey metamorphic rock easily split into flat pieces.");
        DEFINITIONS.put("ADIEU", "A French word meaning goodbye or farewell.");
        DEFINITIONS.put("STARE", "To look fixedly at someone or something with wide open eyes.");
        DEFINITIONS.put("TRAIN", "A series of railroad cars pulled by a locomotive.");
        DEFINITIONS.put("WORLD", "The earth together with all its countries and peoples.");
        DEFINITIONS.put("PRIZE", "A reward given to the winner of a competition.");
        DEFINITIONS.put("BREAK", "To separate into pieces as a result of a blow or strain.");
        DEFINITIONS.put("LIGHT", "The natural agent that stimulates sight and makes things visible.");
        DEFINITIONS.put("BRAVE", "Ready to face and endure danger or pain; showing courage.");
        DEFINITIONS.put("FROST", "A deposit of small white ice crystals formed on a cold surface.");
        DEFINITIONS.put("GLOOM", "Darkness or dimness; a state of depression or despondency.");
        DEFINITIONS.put("STONE", "The hard solid non-metallic mineral matter of which rock is made.");
        DEFINITIONS.put("DRINK", "To take liquid into the mouth and swallow it.");
        DEFINITIONS.put("FLOAT", "To rest or move on the surface of a liquid without sinking.");
        DEFINITIONS.put("JOKER", "A person who makes jokes; or a playing card used as a wild card.");
        DEFINITIONS.put("KNACK", "An acquired skill at performing a task; a natural talent.");
        DEFINITIONS.put("NOBLE", "Belonging to a hereditary class with high social status.");

        TRIVIA.put("HELLO", "The word hello became popular as a telephone greeting suggested by Thomas Edison in 1877!");
        TRIVIA.put("CRANE", "Cranes can live up to 60 years and are known for their elaborate courtship dances.");
        TRIVIA.put("SLATE", "Slate was commonly used for roofing and school writing tablets in the 19th century.");
        TRIVIA.put("ADIEU", "ADIEU is one of the best Wordle starting words because it contains 4 vowels!");
        TRIVIA.put("STARE", "The average person blinks 15-20 times per minute, but staring reduces it to 3-4 times.");
        TRIVIA.put("TRAIN", "The first passenger railway opened in 1825 between Stockton and Darlington, England.");
        TRIVIA.put("WORLD", "The word world comes from Old English weorold, meaning age of man.");
        TRIVIA.put("PRIZE", "The Nobel Prize was established by Alfred Nobel's will in 1895 and first awarded in 1901.");
        TRIVIA.put("BREAK", "The word break has over 50 different meanings in English, one of the most versatile words!");
        TRIVIA.put("LIGHT", "Light travels at approximately 299,792,458 metres per second in a vacuum.");
        TRIVIA.put("BRAVE", "The word brave entered English from Spanish bravo, meaning bold or courageous.");
        TRIVIA.put("FROST", "Robert Frost is one of the most celebrated American poets, famous for nature-themed works.");
        TRIVIA.put("STONE", "The Stone Age lasted roughly 3.4 million years and ended around 3,000 BCE.");
        TRIVIA.put("DRINK", "Humans can survive approximately 3 days without water.");
        TRIVIA.put("FLOAT", "Dead Sea water is so salty that people naturally float without effort.");
        TRIVIA.put("JOKER", "The Joker card was added to the standard deck around 1860 for the game of Euchre.");
        TRIVIA.put("KNACK", "The word knack originally meant a cunning trick before it came to mean a skill.");
        TRIVIA.put("NOBLE", "Noble gases were called inert gases before scientists discovered they can form compounds.");

        EXAMPLES.put("HELLO", "Hello! It is so nice to finally meet you in person.");
        EXAMPLES.put("CRANE", "The construction crane lifted the heavy steel beams with ease.");
        EXAMPLES.put("SLATE", "The teacher wrote the assignment on the slate board.");
        EXAMPLES.put("ADIEU", "She bid him adieu and boarded the train for Paris.");
        EXAMPLES.put("STARE", "It is rude to stare at people in public places.");
        EXAMPLES.put("TRAIN", "We took the morning train to visit our grandparents.");
        EXAMPLES.put("WORLD", "She dreamed of traveling around the world one day.");
        EXAMPLES.put("PRIZE", "He won first prize at the science fair this year.");
        EXAMPLES.put("BREAK", "Let us take a short break before we continue working.");
        EXAMPLES.put("LIGHT", "The morning light streamed beautifully through the window.");
        EXAMPLES.put("BRAVE", "It was brave of her to speak the truth in front of everyone.");
        EXAMPLES.put("FROST", "A thick frost covered the grass on the cold winter morning.");
        EXAMPLES.put("STONE", "He skipped a flat stone across the calm surface of the lake.");
        EXAMPLES.put("DRINK", "She poured herself a cool drink of water after the long run.");
        EXAMPLES.put("FLOAT", "The children loved to float on their backs in the swimming pool.");
        EXAMPLES.put("JOKER", "He was always the joker of the group, making everyone laugh.");
        EXAMPLES.put("KNACK", "She had a real knack for solving complex puzzles quickly.");
        EXAMPLES.put("NOBLE", "It was a noble gesture to donate all the prize money to charity.");
    }

    public String definition(String word) {
        return DEFINITIONS.getOrDefault(word, "A five-letter word used in Wordle.");
    }

    public String trivia(String word) {
        return TRIVIA.getOrDefault(word, "This word was carefully selected for today's puzzle!");
    }

    public String example(String word) {
        return EXAMPLES.getOrDefault(word, "Can you use " + word + " in a sentence today?");
    }
}