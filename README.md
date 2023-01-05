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
