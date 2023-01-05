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

1. MapReduce
2. 
