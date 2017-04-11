FROM centos

# This is just a temp example...
RUN yum install -y vim wget zsh
CMD tail -f /dev/null
