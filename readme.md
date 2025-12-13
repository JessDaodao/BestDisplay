<div align="center">

## BestHealth - 现代化伤害显示

</div>

## 简介

该插件提供了现代化的伤害显示
在玩家攻击实体/其他玩家时，插件会在动作栏显示受击实体/玩家的生命值等信息

注：史山，请捏住鼻子查看源代码

## 其他

### 命令
- `/bh reload` 重载配置文件

### 权限节点
- `besthealth.admin` 管理员命令

### 配置文件

```yaml
# BestHealth配置文件

messages:
  # 插件消息前缀
  prefix: "&8[&bBestHealth&8]&r "

settings:
  display:
    # 是否在实体头顶显示扣血横幅
    damage_above: true
    # 是否在实体头顶显示回血横幅
    healing_above: true
    # 是否在动作栏显示受击实体血量
    health_action_bar: true
  sound:
    # 是否在玩家射中实体时播放音效
    arrow: true
```