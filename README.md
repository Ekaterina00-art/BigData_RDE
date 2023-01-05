# Изучение основ работы с Hadoop в Windows

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

## Запуск примера

1. Создадим проект в IDE и добавим в папки зависимостей следующие папки:
![2022-12-25 (1)](https://user-images.githubusercontent.com/79097818/210796815-7e448b16-de27-4c6f-87fa-af1834bb46e9.png)

2. В проекте создадим файл WordCount.java с кодом:
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
