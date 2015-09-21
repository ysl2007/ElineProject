如何修改代码：

0、到https://github.com/ysl2007/Eline，单击页面右上方的Fork，将代码复制到自己的repo中。
1、clone https://github.com/[your_username]/Eline.git到本地目录
2、创建分支、修改代码、add所有文件、commit提交
3、将新的分支push到github远程代码库中
4、到自己的github的该report主页，点击Pull request，在head fork中选择自己用户的代码库，compare中选择最新的分支，然后提交Pull request。

远程代码库更新之后，更新自己fork到的代码：
0、提交自己代码库的所有修改
1、添加远程仓库：git remote add newVersion git://github.com/base_fork/name.git
2、fetch远程代码库的新版本到本地：git fetch newVersion
3、合并分支：git merge newVersion/master
4、push到自己的远程代码库：git push