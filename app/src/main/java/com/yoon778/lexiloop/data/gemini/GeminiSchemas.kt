package com.yoon778.lexiloop.data.gemini

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

private val schemaJson = Json { ignoreUnknownKeys = false }

internal val purposeResponseSchema: JsonObject = schemaJson.parseToJsonElement(
    """
    {
      "type":"object","additionalProperties":false,
      "properties":{
        "schemaVersion":{"type":"integer","enum":[1]},
        "requestId":{"type":"string"},
        "profile":{"type":"object","additionalProperties":false,
          "properties":{
            "topics":{"type":"array","minItems":1,"maxItems":5,"items":{"type":"object","additionalProperties":false,"properties":{"name":{"type":"string"},"weightPercent":{"type":"integer","minimum":1,"maximum":100}},"required":["name","weightPercent"]}},
            "difficulty":{"type":"string","enum":["BEGINNER","INTERMEDIATE","ADVANCED"]},
            "excludedTopics":{"type":"array","maxItems":10,"items":{"type":"string"}},
            "exampleItems":{"type":"array","minItems":3,"maxItems":5,"items":{"type":"object","additionalProperties":false,"properties":{"expression":{"type":"string"},"targetMeaningKo":{"type":"string"},"topicName":{"type":"string"}},"required":["expression","targetMeaningKo","topicName"]}}
          },"required":["topics","difficulty","excludedTopics","exampleItems"]}
      },"required":["schemaVersion","requestId","profile"]
    }
    """.trimIndent(),
) as JsonObject

internal val recommendationResponseSchema: JsonObject = schemaJson.parseToJsonElement(
    """
    {
      "type":"object","additionalProperties":false,
      "properties":{
        "schemaVersion":{"type":"integer","enum":[1]},
        "requestId":{"type":"string"},
        "items":{"type":"array","minItems":50,"maxItems":50,"items":{"type":"object","additionalProperties":false,
          "properties":{
            "expression":{"type":"string"},
            "baseForm":{"type":["string","null"]},
            "itemType":{"type":"string","enum":["WORD","IDIOM","PHRASAL_VERB","TECH_TERM","EXPRESSION"]},
            "partOfSpeech":{"type":"string","enum":["NOUN","VERB","ADJECTIVE","ADVERB","PREPOSITION","CONJUNCTION","PRONOUN","DETERMINER","INTERJECTION","PHRASE","OTHER"]},
            "targetMeaningKo":{"type":"string"},
            "auxiliaryMeaningsKo":{"type":"array","maxItems":3,"items":{"type":"string"}},
            "topicId":{"type":"string"},
            "difficulty":{"type":"string","enum":["BEGINNER","INTERMEDIATE","ADVANCED"]},
            "example":{"type":"object","additionalProperties":false,"properties":{"template":{"type":"string"},"targetForm":{"type":"string"},"translationKo":{"type":"string"}},"required":["template","targetForm","translationKo"]}
          },"required":["expression","baseForm","itemType","partOfSpeech","targetMeaningKo","auxiliaryMeaningsKo","topicId","difficulty","example"]}}
      },"required":["schemaVersion","requestId","items"]
    }
    """.trimIndent(),
) as JsonObject
