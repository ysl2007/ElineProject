如何修改代码：

甲拥有repository，想要乙也能更新自己的repository，就把乙加入collaborators。

乙初始化的時候， 注意clone URL 是甲的 repo 的 clone URL，即： https://github.com/ysl2007/ElineProject.git
乙在本机添加分支、修改代码（branch、commit、merge）。
乙在本机push时，可以使用自己的账号密码。
如此操作会更新甲的repository（甲仍就可以继续更新自己的repository）
但乙自己的github账户並不會有一份和甲相同的repository（只会有甲的repo链接）

如此不必再使用Fork + Pull request
