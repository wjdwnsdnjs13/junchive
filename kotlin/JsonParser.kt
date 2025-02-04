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

    val json = """
        {
            "name": "John",
            "age": 30,
            "car": null
        }
    """.trimIndent()
//    println(json)
//    println(parse(json))

    val shortJson = """{"name": "John", "age": 30, "car": null}""".trimIndent()
//    println(shortJson)
    println(parse(shortJson))
}

fun parse(json: String): Any {
//    Json 파싱하는 로직 구현
//    문자열 구분, 객체와 배열 처리
    println("json: $json")
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
    var inQuotes = false
    var isKey = true
    var i = 1 // 처음과 끝 제거({})

    while (i < json.length - 1){
        var c = json[i]

        when{
            c == '"' -> {
                c = json[++i]
                // 빈 값일 경우 바로 삽입
                if(c == '"') result[key.toString()] = ""
                while(c != '"'){
                    c = json[i]
                    inQuotes = !inQuotes

                    if(inQuotes && isKey){
                        value.append(c)
                    } else if(!inQuotes && isKey){
                        isKey = false
                    } else if (!inQuotes){
                        value.append(c)
                        println("키는 : $key")
                        println("값은 : $value")
                        result[key.toString()] = parseValue(value.toString())
                        key.clear()
                        value.clear()
                        isKey = true
                    }
                    i++
                    if(i >= json.length) throw IllegalArgumentException("큰 따옴표가 닫히지 않았습니다.")
                }

            }
            c.isDigit() -> {

            }
            c == ':' && !inQuotes -> {
                isKey = false
            }
            c == ',' && !inQuotes -> {
                isKey = true
            }
            else -> {
                // 큰 따옴표 안이 아닌 공백은 전부 무시
                if (inQuotes && isKey){
                    key.append(c)
                }else if(inQuotes){
                    value.append(c)
                }
            }
        }
        i++
    }
    println("result: $result")

    return result
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
        else -> throw IllegalArgumentException("잘 못된 형식의 데이터입니다.")
    }
}
