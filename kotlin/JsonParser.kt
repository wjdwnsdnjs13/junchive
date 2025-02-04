//Json은 key-value 구조의 객체와 여러 value가 들어있는 배열 형태가 있음.
//
//기본적인 데이터 타입
//- String
//- Int
//- Boolean
//- null
//- Map<String, Any>
//- List<Any>
//
//- 문자열 구분
//    - 따옴표로 구분
//- 객체와 배열 구분
//    - {} : 객체
//    - [] : 배열
//- key-value 처리
//    - Key와 Value를 구분해서 Map에 저장
//- 재귀 호출
//    - 중첩 객체나 배열을 파싱하기 위해 재귀적으로 호출함


fun main() {
    val jsonArray = """
        {
            [
                {
                    "name": "John",
                    "age": 30,
                    "car": null
                },
                {
                    "name": "Jane",
                    "age": 25,
                    "car": {
                        "model": "Audi",
                        "year": 2020
                    }
                }
            ]
        }
    """.trimIndent()
//    println(jsonArray)
//    println(parse(jsonArray))

    println("---------------shortJson--------------------")

    val shortJson = """{"name": "John", "age": 30}""".trimIndent()

//    println(shortJson)

    println(fromJson(shortJson))

    println("-----------------Json-----------------")

    val json = """
        {
            "name": "John",
            "age": 30,
            "car": null
        }
    """.trimIndent()
//    println(json)
    println(fromJson(json))


    println("----------------toJson---------------")
    val data: Map<String, Any?> = mapOf(
        "name" to "John Doe",
        "age" to 30L,
        "address" to mapOf(
            "street" to "123 Main St",
            "city" to "Anytown",
            "postalCode" to "12345"
        ),
        "phones" to listOf(
            mapOf("type" to "home", "number" to "123-456-7890"),
            mapOf("type" to "work", "number" to "098-765-4321")
        ),
        "isActive" to true,
        "nullValue" to null
    )
    println(toJson(data))
}

fun toJson(data: Map<String, Any?>): String {
    return data.entries.joinToString(
        ",", "{", "}"
    )
}



fun fromJson(json: String): Any {
//    Json 파싱하는 로직 구현
//    문자열 구분, 객체와 배열 처리
    val json = json.trim() // 공백 제거
    return when {
        json.startsWith("{") -> parseObject(json)
        json.startsWith("[") -> parseArray(json)
        else -> throw IllegalArgumentException("올바르지 않은 포멧의 데이터입니다.")
    }
}

//    객체 파싱
fun parseObject(json: String): Map<String, Any>{
    val result = mutableMapOf<String, Any>()
    var key = StringBuilder()
    var value = StringBuilder()
    var isKey = true
    var i = 1 // 처음과 끝 제거({})

    while (i < json.length - 1){
        var c = json[i]

        when{
            c == '"' -> {
                c = json[++i]
                // 빈 값일 경우 바로 삽입
                if(c == '"') result[key.toString()] = ""
                if(!isKey) value.append('"')
                var playCount = 0
                while(c != '"'){
                    playCount++
                    c = json[i]

                    if(isKey){
                        key.append(c)
                    } else {
                        value.append(c)
                    }
                    i++
                    if(i > json.length) throw IllegalArgumentException("큰 따옴표가 닫히지 않았습니다.")
                    if(playCount >= 10_000) throw IllegalArgumentException("너무 길이가 깁니다.")
                }
                if(!isKey){
                    result[key.toString()] = parseValue(value.toString())
                    key.clear()
                    value.clear()
                    isKey = !isKey
                } else {
//                    키의 경우에는 맨 마지막 큰 따옴표 제거해줘야 함.
                    key = StringBuilder(key.substring(0, key.length - 1))
                }
//                i가 +1된 상태이므로 돌려줌
                i--
            }
            c.isDigit() -> {
                value.append(c)
                c = json[++i]
                // 한 자리일 경우 바로 삽입
                if(c == ',' || c == '}') result[key.toString()] = json[i - 1]
                var playCount = 0
                while(c != ',' && c != '}'){
                    playCount++
                    c = json[i]
                    if(c == ',' || c == '}') break
                    value.append(c)

                    i++

                    if(i > json.length) throw IllegalArgumentException("value가 끝나지 않았습니다.")
                    // 우선 int 값 까지만 받기
                    if(playCount >= 9) throw IllegalArgumentException("너무 길이가 깁니다.")
                }
                // 숫자 양 옆의 공백 제거
                result[key.toString()] = parseValue(value.toString().trim())
                key.clear()
                value.clear()
                isKey = !isKey

//                i가 +1된 상태이므로 돌려줌
                i--

            }
            c == '-' -> {
//                음수 구현
            }
            c == ':' -> {
                isKey = false
            }
            c == ',' -> {
                isKey = true
            }
//            어디까지 범위로 해서 다시 넘겨 받아야 할지 고민...
//            json의 길이를 리턴 받아서 끊긴 범위부터 이어가게 해도 될 것 같음.
//            c == '{' -> {
//                parseObject(json.substring(i))
//            }
//            null
            c == 'n' -> {
                if(isKey){
                    throw IllegalArgumentException("키가 없습니다.")
                }
                result[key.toString()] = parseValue(json.substring(i, i + 4))
                i += 3
            }
//            큰 따옴표 밖의 띄어쓰기와 줄바꿈은 무시
            c == ' ' || c == '\n' -> {}
            c == '}' -> break
            c == '{' -> {
                val blockCloseCount = codeBlockCloseCountCalc('{', '}', json, i)
                result[key.toString()] = parseObject(json.substring(i, blockCloseCount))
                i = blockCloseCount
            }
            else -> {
                throw IllegalArgumentException("올바르지 않은 데이터 형식입니다.")
            }
        }
        i++
    }

    return result
}

//코드 블럭 카운트 계산
fun codeBlockCloseCountCalc(startPoint: Char, endPoint: Char, json: String, i: Int): Int {
    var count = 1
    var i = i + 1

    while(count != 0){
        val c = json[i++]
        if(c == startPoint) count++
        else if(c == endPoint) count--

        if(i > json.length) throw IllegalArgumentException("value가 끝나지 않았습니다.")
        // 우선 int 값 까지만 받기
        if(count >= 100_000) throw IllegalArgumentException("너무 길이가 깁니다.")
    }
    println("codeBlockCloseCount: $i")
    return i
}

//    배열 파싱
fun parseArray(json: String): List<Any> {
    var result = mutableListOf<Any>()
    var value = StringBuilder()
    var inQuotes = false
    var i = 1 // 처음과 끝 제거([])

    while (i < value.length - 1){
        val c = json[i]

        when{
            c == '"' -> inQuotes = !inQuotes
            c == ',' && !inQuotes -> {
                result.add(parseValue(value.toString()))
                value = StringBuilder()
            }
            else -> value.append(c)
        }
        i++
    }
//    value가 존재하면 추가
    if (value.isNotEmpty()){
        result.add(parseValue(value.toString()))
    }

    return result
}

//    값 변환
fun parseValue(value: String): Any {
    val intRegex = "\\d{1,}".toRegex()
    println("value: $value")
    return when{
//        String
        value.startsWith("\"") && value.endsWith("\"") -> value.substring(1, value.length - 1)
//        Double
        value.contains(".") -> value.toDouble()
//        true
        value.equals("true") -> value.toBoolean()
//        false
        value.equals("false") -> value.toBoolean()
//        Int
        intRegex.matches(value) -> value.toInt()
//        null -> 아직 잘 모르겠음.
        value.equals("null") -> "null"
        else -> throw IllegalArgumentException("잘 못된 형식의 데이터입니다.")
    }
}
