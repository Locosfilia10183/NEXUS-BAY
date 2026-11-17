# Network

| Edition | Protocol | Port |
|---|---|---:|
| Java | TCP | 25565 |
| Bedrock | UDP | 19132 |

Allow both in the VPS provider firewall and UFW if enabled:

```bash
sudo ufw allow 25565/tcp
sudo ufw allow 19132/udp
sudo ufw status
```

Geyser's Bedrock listener uses UDP. Open UDP 19132, not TCP only. Initially connect using the VPS public IP; add a domain after connection tests succeed.
