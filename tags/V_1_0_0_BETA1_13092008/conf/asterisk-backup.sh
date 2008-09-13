mkdir ~/astbackup
mkdir ~/astbackup/astetcdir
cp -R /etc/asterisk/* ~/astbackup/astetcdir
mkdir ~/astbackup/astmoddir
cp -R /usr/lib/asterisk/modules/* ~/astbackup/astmoddir
mkdir ~/astbackup/astvarlibdir
cp -R /var/lib/asterisk/* ~/astbackup/astvarlibdir
mkdir ~/astbackup/astagidir
cp -R /var/lib/asterisk/agi-bin/* ~/astbackup/astagidir
mkdir ~/astbackup/astspooldir
cp -R /var/spool/asterisk/* ~/astbackup/astspooldir
mkdir ~/astbackup/astlogdir
cp -R /var/log/asterisk/* ~/astbackup/astlogdir

