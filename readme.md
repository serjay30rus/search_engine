# Search Engine

Поисковый движок Search Engine представляет из себя
Spring-приложение (JAR-файл, запускаемый на любом сервере или
компьютере), работающее с локально установленной базой данных MySQL,
имеющее простой веб-интерфейс и API, через который им можно управлять и
получать результаты поисковой выдачи по запросу. 

### Принцип работы поискового движка:
1. В конфигурационном файле перед запуском приложения задаются
адреса сайтов, по которым движок должен будет осуществлять поиск.
2. Поисковый движок должен самостоятельно обходить все страницы
заданных сайтов и индексировать их таким образом, чтобы потом по
любому поисковому запросу находить наиболее релевантные
(подходящие) страницы.
3. Пользователь присылает запрос через API движка. Запрос — это набор
слов, по которым нужно найти страницы сайта.
4. Запрос определённым образом трансформируется в список слов,
переведённых в базовую форму (например, для существительных —
именительный падеж, единственное число).
5. В индексе ищутся те страницы, на которых встречаются все эти слова.
6. Результаты поиска ранжируются, сортируются и отдаются пользователю.