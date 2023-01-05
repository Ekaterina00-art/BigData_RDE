# Изучение основ работы с Hadoop в Windows

## Hadoop
**Hadoop** - свободно распространяемый набор утилит, библиотек и фреймворк для разработки и выполнения распределённых программ обработки данных и хранения данных, работающих на кластерах из сотен и тысяч узлов.

Hadoop состоит из четырёх модулей:
* Hadoop Common - набор инфраструктурных программных библиотек и утилит, используемых для других модулей и родственных проектов;
* HDFS - распределённая файловая система;
* YARN - система для планирования заданий и управления кластером;
* Hadoop MapReduce - платформа программирования и выполнения распределённых MapReduce-вычислений.

## Сборка Hadoop

1. Устанавливаем JDK

2. Скачиваем Hadoop. Скачать можно по ссылке - https://hadoop.apache.org/releases.html

3. Извлекаем hadoop-3.1.0.tar.gz в С:\hadoop-3.1.0. Добавляем пути JDK и Hadoop в переменные окружения.
![2022-12-26 (2)](https://user-images.githubusercontent.com/79097818/210790900-e1ffe995-de61-4ea2-8a28-d0a76b61eac7.png)

4. Извлекаем и устанавливаем Java в C:\Java. Чтобы проверить прошла ли установка успешно, вводим в командную строку - "javac -version".

## Конфигурация Hadoop 

1. Отредактируем файл C:/Hadoop-3.1.0/etc/hadoop/core-site.xml и изменим блок с конфигурацией следующим образом.

```html
<configuration> 
  <property> 
    <name>fs.defaultFS</name> 
    <value>hdfs://localhost:9000</value> 
  </property> 
</configuration>
```

2. Отредактируем C:/Hadoop-3.1.0/etc/hadoop/mapred-site.xml и вставим туда следующий блок:
```html
<configuration> 
   <property> 
      <name>mapreduce.framework.name</name>
      <value>yarn</value> 
   </property> 
</configuration> 
```

3. Создадим папку data в C:/Hadoop-3.1.0, а в ней папки datanode и namenode.

4. Отредактируем файл C:/Hadoop-3.1.0/etc/hadoop/hdfs-site.xml и отредактируем блок с конфигурацией следующим образом:
```html
<configuration>
       <property>
             <name>dfs.replication</name>
             <value>1</value>
       </property>
       <property>
              <name>dfs.namenode.name.dir</name>
              <value>file:///C:/hadoop-3.1.0/data/namenode</value>
       </property>
       <property>
              <name>dfs.datanode.data.dir</name>
              <value>file:///C:/hadoop-3.1.0/data/datanode</value>
       </property>
</configuration>
```

5. Отредактируем файл C:/Hadoop-3.1.0/etc/hadoop/yarn-site.xml и отредактируем блок с конфигурацией следующим образом:
```html
<configuration>
<!-- Site specific YARN configuration properties -->
 <property>
    <name>yarn.nodemanager.aux-services</name>
	<value>mapreduce_shuffle</value>
</property>
<property>
    <name>yarn.nodemanager.auxservices.mapreduce.shuffle.class</name> 
	<value>org.apache.hadoop.mapred.ShuffleHandler</value>
</property>
</configuration>
```

## Тестирование

1. В командной строке перейдем в директорию C:/Hadoop-3.1.0/sbin и запустим start-all.cmd. Результатом должны быть следующие запущенные 4 окна:
![2022-12-22](https://user-images.githubusercontent.com/79097818/210794773-2a728892-c108-49b8-9257-09980c80c39c.png)

2. Открываем http://localhost:8088. И если мы правильно установили, то должно открыться:
![2023-01-05](https://user-images.githubusercontent.com/79097818/210796301-6d108081-c1db-402c-976b-3972aa0d08b0.png)

## Запуск примера №1

1. Создадим проект в IDE и добавим в папки зависимостей следующие папки:
![2022-12-25 (1)](https://user-images.githubusercontent.com/79097818/210796815-7e448b16-de27-4c6f-87fa-af1834bb46e9.png)

2. В проекте создадим файл WordCount.java с кодом (считает кол-во слов):
```java
import java.io.IOException;
import java.util.*;

//Packages available in hadoop-common jar
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
//Packages available in hadoop-mapreduce-client-core jar
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class WordCount {
	
	public static class Map extends Mapper<LongWritable, Text, Text, IntWritable>{
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			
			String line = value.toString();
			
			StringTokenizer token = new StringTokenizer(line);
			while(token.hasMoreTokens()) {
				value.set(token.nextToken());
				context.write(value, new IntWritable(1));
			}
		}
	}
	
	public static class Reduce extends Reducer<Text, IntWritable ,Text, IntWritable>{
		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			int sum = 0;
			for(IntWritable x : values) {
				sum += x.get();
			}
			context.write(key, new IntWritable(sum));
		}
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		Configuration conf = new Configuration();
		
		Job job = Job.getInstance(conf,"WordCount");
		
		job.setJarByClass(WordCount.class);
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		Path outputPath = new Path(args[1]);
		
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		outputPath.getFileSystem(conf).delete(outputPath, true);
		
		System.exit(job.waitForCompletion(true) ? 0 : 1);
		
	}
}
```
3. Запустим команду jar -cvf WordCount.jar -C WordCount/, в результате которой в этой папке появится собранный .jar
![imgonline-com-ua-Replace-color-c4bO2jiiZooeYH3](https://user-images.githubusercontent.com/79097818/210802122-75fbabea-c13d-4dfe-80ec-92049c9d71c4.jpg)

4. При запущенном Hadoop запустим две команды, первая создаст папку в файловой системе Hadoop, вторая – переместит в неё файл hadoop fs -mkdir /input_dir hadoop fs -put C:/input_file.txt /input_dir 
В файле **"input_file.txt"**:
```
This is the example text file for word count example also knows as hello world example of the Hadoop ecosystem.
This example is written for the examples article of java code geek
The quick brown fox jumps over the lazy dog.
The above line is one of the most famous lines which contains all the English language alphabets.
```

5. Вывод программы<br>
![2022-12-26 (1)](https://user-images.githubusercontent.com/79097818/210803141-33da5a88-a956-4cfa-8896-ffd77e625fbe.png)

## Запуск примера №2

Само задание:
```
Построение инверсного индекса для списка документов. Входные данные хранятся в файлах. 
Входные данные – набор текстов (аналогично задаче подсчета числа слов). 
Результат – пары: <ключ, значение>, где ключ – слово, значение – Список пар (Например - имя документа, индекс документа). 
```
По заданию будем использовать MapReduce. Для начала определимся, что это такое.

> Основной источник - Том Уайт Hadoop: Подробное руководство. — СПб.: Питер, 2013. — 672 с.: ил. — (Серия «Бестселлеры O’Reilly»)

1. MapReduce

**MapReduce** — это фреймворк для вычисления некоторых наборов распределенных задач с использованием большого количества компьютеров (называемых «узлами»), образующих кластер. Каждая фаза использует в качестве входных и выходных данных пары "ключ-значение".

Работа MapReduce состоит из двух шагов: Map (отображение) и Reduce(свертка).

На Map-шаге происходит предварительная обработка входных данных. Для этого один из компьютеров (называемый главным узлом — master node) получает входные данные задачи, разделяет их на части и передает другим компьютерам (рабочим узлам — worker node) для предварительной обработки.

На Reduce-шаге происходит свёртка предварительно обработанных данных. Главный узел получает ответы от рабочих узлов и на их основе формирует результат — решение задачи, которая изначально формулировалась.

Преимущество MapReduce заключается в том, что он позволяет распределенно производить операции предварительной обработки и свертки. Операции предварительной обработки работают независимо друг от друга и могут производиться параллельно (хотя на практике это ограничено источником входных данных и/или количеством используемых процессоров). Аналогично, множество рабочих узлов может осуществлять свертку — для этого необходимо только чтобы все результаты предварительной обработки с одним конкретным значением ключа обрабатывались одним рабочим узлом в один момент времени.

2. Инвертированный индекс

**Инвертированный индекс (Inverted Index)** - это структура данных, на которую должны опираться почти все поисковые системы, поддерживающие полнотекстовый поиск. На основе структуры индекса, заданного термина, можно получить список документов, содержащих термин.

Проблемы в веб-поиске в основном делятся на три части:
* Сканирование (сбор веб-контента), веб-сканер, сбор данных;
* Индексирование (построение инвертированного индекса), построение структуры инвертированного индекса на основе большого количества данных;
* Поиск (ранжирование документов по запросу), индекс и результаты поиска по поисковому слову.

3. Реализован алгоритм на языке программирования JAVA, который представлен в файле "InvertedIndex.java". В ходе запустила выполнение MapReduce задачи в Hadoop.

4. Результат программы
```
hadoop fs -mkdir /input
PS C:\lab5> hadoop fs -put f:/input/wuhia_novels.txt /input
PS C:\lab5> hadoop fs -put f:/input/wuhia_novels2.txt /input
PS C:\lab5> hadoop jar InvertseIndex.jar /input /output 
Warning: $HADOOP_HOME is deprecated

22/12/26 19:19:01 WARN mapred.JobClient: Use Generic0ptionsParser for parsing the arguments. Applications should implement Tool for the same.
22/12/26 19:19:02  INFO input.FileInputFormat: Total input paths to process : 21
22/12/26 19:19:02 INFO mapred.JobClient: Running job: job_201507041926_0057
22/12/26 19:19:02 INFO mapred.JobClient: map 0% reduce 0% 
22/12/26 19:19:02 INFO mapred.JobClient: map 30% reduce 0%
22/12/26 19:19:03 INFO mapred.JobClient: map 32% reduce 0%
22/12/26 19:19:03 INFO mapred.JobClient: map 45% reduce 0%
22/12/26 19:19:04 INFO mapred.JobClient: map 49% reduce 0%
22/12/26 19:19:04 INFO mapred.JobClient: map 68% reduce 0%
22/12/26 19:19:04 INFO mapred.JobClient: map 81% reduce 0% 
22/12/26 19:19:05 INFO mapred.JobClient: map 88% reduce 8%
22/12/26 19:19:05 INFO mapred.JobClient:  map 100% reduce 100%
22/12/26 19:19:05  INFO mapred.JobClient: Counters: 49
        File System Counters
                FILE: Number of bytes read=11113
                FILE: Number of bytes written=433156
                FILE: Number of read operations=0
                FILE: Number of large read operations=0
                FILE: Number of write operations=0
                HDFS: Number of bytes read=2366
                HDFS: Number of bytes written=4491
                HDFS: Number of read operations=9
                HDFS: Number of large read operations=0
                HDFS: Number of write operations=2
        Job Counters
                Launched map tasks=2
                Launched reduce tasks=1
                Data-local map tasks=2
                Total time spent by all maps in occupied slots (ms)=12102
                Total time spent by all reduces in occupied slots (ms)=4558
                Total time spent by all map tasks (ms)=12102
                Total time spent by all reduce tasks (ms)=4558
                Total vcore-milliseconds taken by all map tasks=12102
                Total vcore-milliseconds taken by all reduce tasks=4558
                Total megabyte-milliseconds taken by all map tasks=12392448
                Total megabyte-milliseconds taken by all reduce tasks=4667392
        Map-Reduce Framework
                Map input records=70
                Map output records=415
                Map output bytes=10277
                Map output materialized bytes=11119
                Input split bytes=224
                Combine input records=0
                Combine output records=0
                Reduce input groups=131
                Reduce shuffle bytes=11119
                Reduce input records=415
                Reduce output records=131
                Spilled Records=830
                Shuffled Maps =2
                Failed Shuffles=0
                Merged Map outputs=2
                GC time elapsed (ms)=201
                CPU time spent (ms)=1747
                Physical memory (bytes) snapshot=706154496
                Virtual memory (bytes) snapshot=865771520
                Total committed heap usage (bytes)=510132224
        Shuffle Errors
                BAD_ID=0
                CONNECTION=0
                IO_ERROR=0
                WRONG_LENGTH=0
                WRONG_MAP=0
                WRONG_REDUCE=0
        File Input Format Counters
                Bytes Read=2142
        File Output Format Counters
                Bytes Written=4491
PS C:\lab5> hadoop dfs -cat /output/*
Warning: $HADOOP_HOME is deprecated

abhorrent   wuhia_novels.txt: 1
absorb   wuhia_novels2.txt: 1
adler   wuhia_novels.txt: 1
admirably   wuhia_novels.txt: 1
akin   wuhia_novels.txt: 1
all   wuhia_novels.txt: 1 wuhia_novels2.txt: 1
always   wuhia_novels.txt: 1
and   wuhia_novels.txt: 2 wuhia_novels2.txt: 1
any   wuhia_novels2.txt: 2
around   wuhia_novels2.txt: 1
attention   wuhia_novels2.txt: 1
away   wuhia_novels2.txt: 1
balanced   wuhia_novels.txt: 1
bohemian   wuhia_novels2.txt: 1
but   wuhia_novels.txt: 1
cold   wuhia_novels.txt: 1
complete   wuhia_novels2.txt: 1
drifted   wuhia_novels2.txt: 1
each   wuhia_novels2.txt: 1
eclipses   wuhia_novels.txt: 1
emotion   wuhia_novels.txt: 1
emotions   wuhia_novels.txt: 1
establishment   wuhia_novels2.txt: 1
every   wuhia_novels2.txt: 1
eyes   wuhia_novels.txt: 1
felt   wuhia_novels.txt: 1
finds   wuhia_novels2.txt: 1
first   wuhia_novels2.txt: 1
for   wuhia_novels.txt: 1
form   wuhia_novels2.txt: 1
from   wuhia_novels2.txt: 1
had   wuhia_novels2.txt: 2
happiness   wuhia_novels2.txt: 1
have   wuhia_novels.txt: 1
he   wuhia_novels.txt: 1
heard   wuhia_novels.txt: 1
her   wuhia_novels.txt: 2
him   wuhia_novels.txt: 1
himself   wuhia_novels2.txt: 1
his   wuhia_novels.txt: 2  wuhia_novels2.txt: 2
holmes   wuhia_novels.txt: 1  wuhia_novels2.txt: 2
home-centred   wuhia_novels2.txt: 1
i   wuhia_novels.txt: 1  wuhia_novels2.txt: 1
in   wuhia_novels.txt: 1
interests   wuhia_novels2.txt: 1
irene   wuhia_novels.txt: 1
is   wuhia_novels.txt: 1
it   wuhia_novels.txt: 1
lately   wuhia_novels2.txt: 1
love   wuhia_novels.txt: 1
man   wuhia_novels2.txt: 1
marriage   wuhia_novels2.txt: 1
my   wuhia_novels2.txt: 3
name   wuhia_novels.txt: 1
not   wuhia_novels.txt: 1
of   wuhia_novels.txt: 1  wuhia_novels2.txt: 3
one   wuhia_novels.txt: 1
own   wuhia_novels2.txt: 2
particularly   wuhia_novels.txt: 1
precise   wuhia_novels.txt: 1
predominates  wuhia_novels.txt: 1
rise   wuhia_novels2.txt: 1
she   wuhia_novels.txt: 2
sherlock   wuhia_novels.txt: 1
that  wuhia_novels.txt: 2
the   wuhia_novels.txt: 2 wuhia_novels2.txt: 2
to   wuhia_novels.txt: 3  wuhia_novels2.txt: 1
under   wuhia_novels.txt: 1
up   wuhia_novels2.txt: 1
us   wuhia_novels2.txt: 1
was   wuhia_novels.txt: 1
which  wuhia_novels2.txt: 1
while   wuhia_novels2.txt: 1
who   wuhia_novels2.txt: 2
whole   wuhia_novels.txt: 1  wuhia_novels2.txt: 1
with   wuhia_novels2.txt: 1
woman   wuhia_novels.txt: 1
```
