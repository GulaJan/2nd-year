#include <unistd.h> //getopt
#include <stdlib.h> //exit
#include <errno.h>  //perror
#include <arpa/inet.h> //inet_pton
#include <strings.h> //bzero
#include <sys/socket.h>
#include <netinet/ip.h>
#include <netinet/ip_icmp.h>
#include <netinet/udp.h>
#include <sys/time.h>
#include <stdio.h>
#include <signal.h>
#include <netinet/ip6.h>
#include <netinet/icmp6.h>

#define DEST_PORT_ROOT 33453
#define BUFSIZE 2048

void trace(struct sockaddr_in *dest, int first_ttl, int max_ttl);
int receive_icmp(int recvfd, uint16_t src_port, uint16_t dest_port, struct timeval start_t);

void trace6(struct sockaddr_in6 *sin_dest, int first_ttl, int max_ttl);
int receive_icmp6(int recvfd, uint16_t src_port, uint16_t dest_port, struct timeval start_t);

const char usage[] = "Usage: trace [-f first_ttl] [-m max_ttl] <ip-address>";
char ip_strbuf[INET6_ADDRSTRLEN];
char recvbuf[BUFSIZE];
int alarm_call = 0;

int main(int argc, char **argv) {
    int ch, first_ttl = 1, max_ttl = 30;
    char *ip_to_trace;

    // aby getopt nevypisoval chyby
    opterr = 0;

    while ((ch = getopt(argc, argv, "f:m:")) != -1) {
        switch (ch) {
            case 'f':
                if ((first_ttl = atoi(optarg)) < 1) {
                    perror("first_ttl must be >= 1");
                    exit(1);
                }
                break;
            case 'm':
                if ((max_ttl = atoi(optarg)) > 255) {
                    perror("max_ttl must be <= 255");
                    exit(1);
                }
                break;
            case '?':
                perror(usage);
                exit(1);
                break;
        }
    }

    if (argc != optind + 1) {
        perror(usage);
        exit(1);
    }

    ip_to_trace = argv[optind];

    struct sockaddr_in dest;
    struct sockaddr_in6 dest6;

    bzero(&dest, sizeof(dest));
    bzero(&dest6, sizeof(dest6));

    if (inet_pton(AF_INET, ip_to_trace, &(dest.sin_addr)) == 1) {
        dest.sin_family = AF_INET;
        trace(&dest, first_ttl, max_ttl);
    } else if (inet_pton(AF_INET6, ip_to_trace, &(dest6.sin6_addr)) == 1) {
        dest6.sin6_family = AF_INET6;
        trace6(&dest6, first_ttl, max_ttl);
    } else {
        perror("Wrong ip address.");
        exit(1);
    }


    return 0;
}


int create_socket(int family, int type, int protocol) {
    int desc = 0;
    if ((desc = socket(family, type, protocol)) == -1) {
        perror("Unable to create socket.");
        exit(2);
    } else {
        return desc;
    }
}

uint16_t get_port_number(int sockfd, struct sockaddr *sin) {
    socklen_t addrlen = sizeof(*sin);
    if(getsockname(sockfd, sin, &addrlen) == 0)
    {
        if (sin->sa_family == AF_INET)
            return ntohs(( (struct sockaddr_in *) sin)->sin_port);
        else if (sin->sa_family == AF_INET6)
            return ntohs(( (struct sockaddr_in6 *) sin)->sin6_port);
        else
            exit(1);
    }
    else{
        perror("getsockname error.");
        exit(1);
    }

}


void handle_sigalrm(int signo) {
    alarm_call = 1;
    return;
}

void trace(struct sockaddr_in *sin_dest, int first_ttl, int max_ttl) {
    int sendfd = create_socket(AF_INET, SOCK_DGRAM, 0);
    int recvfd = create_socket(AF_INET, SOCK_RAW, IPPROTO_ICMP);

    struct sockaddr_in sin_src;
    bzero(&sin_src, sizeof(sin_src));
    sin_src.sin_family = AF_INET;
    // zvol nahodny port
    sin_src.sin_port = 0;
    if (bind(sendfd, (struct sockaddr *) &sin_src, sizeof(sin_src)) == -1) {
        perror("Bind unsuccessful.");
        exit(2);
    }

    struct sockaddr_in help_sin;
    uint16_t src_port = get_port_number(sendfd,(struct sockaddr *) &help_sin);
    uint16_t dest_port;

    struct sigaction sact = {
        .sa_handler = handle_sigalrm,
        .sa_flags = 0,
    };
    sigaction(SIGALRM, &sact, NULL);

    int ttl;
    for (ttl = first_ttl; ttl <= max_ttl; ++ttl) {
        printf("%-3d", ttl);

        if (setsockopt(sendfd, IPPROTO_IP, IP_TTL, &ttl, sizeof (int)) == -1) {
            perror("Error while setting ttl.");
            exit(1);
        }

        dest_port = DEST_PORT_ROOT + ttl;
        sin_dest->sin_port = htons(dest_port);

        struct timeval start_t;
        gettimeofday(&start_t, NULL);
        if (sendto(sendfd, NULL, 0, 0, (struct sockaddr *) sin_dest, sizeof(*sin_dest)) == -1) {
            perror("Sending UDP datagram failed.");
            exit(1);
        }

        if (receive_icmp(recvfd, src_port, dest_port, start_t)) {
            break;
        }
    }
}


int receive_icmp(int recvfd, uint16_t src_port, uint16_t dest_port, struct timeval start_t) {
    struct icmp *rec_icmp;
    struct ip *rec_ip, *rec_iph;
    struct udphdr *rec_udp;

    struct sockaddr sa_recv;
    socklen_t recv_len;
    int n, icmp_offset, udp_offset, msg_received = 0;

    alarm_call = 0;
    alarm(2);
    while (!msg_received && !alarm_call) {
        recv_len = sizeof(sa_recv);
        n = recvfrom(recvfd, recvbuf, sizeof (recvbuf), 0, &sa_recv, &recv_len);

        // alarm sa zavolal
        if (n < 0) {
            if (errno == EINTR) {
                continue;
            }
            else {
                perror("Error reading icmp respond.");
                exit(1);
            }
        }

        /* recvbuf ma obsahovat
            IP Header (20bytes), ICMP header (8bytes), IP Header of datagram that caused error (20bytes), UDP Header (8bytes)
        */

        rec_ip = (struct ip *) recvbuf;
        // ip_hl obsahuje velkost ip hlavicky ako pocet 32-bitovych slov
        icmp_offset = rec_ip->ip_hl * 4;

        // chceme este aspon 12 validnych bytov - 8 na ICMP header a 4 na dlzku IP headeru
        if (n - icmp_offset < 12)
            continue;
        rec_icmp = (struct icmp*) (recvbuf + icmp_offset);
        rec_iph = (struct ip*) (recvbuf + icmp_offset + 8);
        udp_offset = icmp_offset + 8 + rec_iph->ip_hl * 4;

        // este 8 bytov na udp header
        if (n - udp_offset < 8)
            continue;
        rec_udp = (struct udphdr*) (recvbuf + udp_offset);

        // skontrolujeme, ci ide o icmp odpoved, ktoru hladame
        if (rec_iph->ip_p == IPPROTO_UDP &&
              rec_udp->source == htons (src_port) &&
              rec_udp->dest == htons (dest_port))
        {
            msg_received = 1;
        }
    }

    alarm(0);
    // prisli sme do timeoutu
    if (msg_received == 0) {
        printf("*\n");
        return 1;
    }

    struct timeval end_t;
    gettimeofday(&end_t, NULL);
    double diff = (double)(end_t.tv_usec - start_t.tv_usec) / 1000 + (double)(end_t.tv_sec - start_t.tv_sec) * 1000;

    printf("%s   ", inet_ntop(
            sa_recv.sa_family,
            &((struct sockaddr_in *) &sa_recv)->sin_addr,
            ip_strbuf,
            INET6_ADDRSTRLEN
        )
    );

    if (rec_icmp->icmp_type == ICMP_TIMXCEED && rec_icmp->icmp_code == ICMP_TIMXCEED_INTRANS) {
        printf("%.3f ms\n", diff);
        return 0;
    } else if (rec_icmp->icmp_type == ICMP_UNREACH) {

        switch (rec_icmp->icmp_code)
        {
            case ICMP_UNREACH_PORT:
                printf("%.3f ms\n", diff);
                break;
            case ICMP_UNREACH_PROTOCOL:
                printf("P!\n");
                break;
            case ICMP_UNREACH_HOST:
                printf("H!\n");
                break;
            case ICMP_UNREACH_NET:
                printf("N!\n");
                break;
            case ICMP_UNREACH_FILTER_PROHIB:
                printf("X!\n");
                break;
            default:
                printf("?!\n");
                break;
        }
    } else {
        printf("?!\n");
    }

    return 1;
};


void trace6(struct sockaddr_in6 *sin_dest, int first_ttl, int max_ttl) {
    int sendfd = create_socket(AF_INET6, SOCK_DGRAM, 0);
    int recvfd = create_socket(AF_INET6, SOCK_RAW, IPPROTO_ICMPV6);

    struct icmp6_filter myfilt;
    ICMP6_FILTER_SETBLOCKALL (&myfilt);
    ICMP6_FILTER_SETPASS (ICMP6_TIME_EXCEEDED, &myfilt);
    ICMP6_FILTER_SETPASS (ICMP6_DST_UNREACH, &myfilt);
    setsockopt (recvfd, IPPROTO_IPV6, ICMP6_FILTER, &myfilt, sizeof (myfilt));

    struct sockaddr_in6 sin_src;
    bzero(&sin_src, sizeof(sin_src));
    sin_src.sin6_family = AF_INET6;
    // zvol nahodny port
    sin_src.sin6_port = 0;
    if (bind(sendfd, (struct sockaddr *) &sin_src, sizeof(sin_src)) == -1) {
        perror("Bind unsuccessful.");
        exit(2);
    }

    struct sockaddr_in6 help_sin;
    uint16_t src_port = get_port_number(sendfd, (struct sockaddr *) &help_sin);
    uint16_t dest_port;

    struct sigaction sact = {
        .sa_handler = handle_sigalrm,
        .sa_flags = 0,
    };
    sigaction(SIGALRM, &sact, NULL);

    int ttl;
    for (ttl = first_ttl; ttl <= max_ttl; ++ttl) {
        printf("%-3d", ttl);

        if (setsockopt(sendfd, IPPROTO_IPV6, IPV6_UNICAST_HOPS, &ttl, sizeof (int)) == -1) {
            perror("Error while setting hops.");
            exit(1);
        }

        dest_port = DEST_PORT_ROOT + ttl;
        sin_dest->sin6_port = htons(dest_port);

        struct timeval start_t;
        gettimeofday(&start_t, NULL);
        if (sendto(sendfd, NULL, 0, 0, (struct sockaddr *) sin_dest, sizeof(*sin_dest)) == -1) {
            perror("Sending UDP datagram failed.");
            exit(1);
        }


        if (receive_icmp6(recvfd, src_port, dest_port, start_t)) {
            break;
        }

    }

}


int receive_icmp6(int recvfd, uint16_t src_port, uint16_t dest_port, struct timeval start_t) {
    struct icmp6_hdr *rec_icmp6;
    struct ip6_hdr *rec_iph6;
    struct udphdr *rec_udp;

    struct sockaddr sa_recv;
    socklen_t recv_len;
    int n, msg_received = 0;

    alarm_call = 0;
    alarm(2);
    while (!msg_received && !alarm_call) {
        recv_len = sizeof(sa_recv);
        n = recvfrom(recvfd, recvbuf, sizeof (recvbuf), 0, &sa_recv, &recv_len);

        // alarm sa zavolal
        if (n < 0) {
            if (errno == EINTR) {
                continue;
            }
            else {
                perror("Error reading icmp respond.");
                exit(1);
            }
        }

        /* recvbuf ma obsahovat
            ICMPv6 header (8bytes), IPv6 Header of datagram that caused error (40bytes), UDP Header (8bytes)
        */


        // chceme este aspon 54 validnych bytov - 8 na ICMPv6 header a 40 na dlzku IP headeru, 8 na UDP header
        if (n < 54)
            continue;

        rec_icmp6 = (struct icmp6_hdr*) recvbuf;
        rec_iph6 = (struct ip6_hdr*) (recvbuf + 8);
        rec_udp = (struct udphdr*) (recvbuf + 48);
        // skontrolujeme, ci ide o icmp pre nase porty
        if (rec_iph6->ip6_nxt == IPPROTO_UDP &&
              rec_udp->source == htons (src_port) &&
              rec_udp->dest == htons (dest_port))
        {
            msg_received = 1;
        }
    }

    alarm(0);

     // prisli sme do timeoutu
    if (msg_received == 0) {
        printf("*\n");
        return 1;
    }

    struct timeval end_t;
    gettimeofday(&end_t, NULL);
    double diff = (double)(end_t.tv_usec - start_t.tv_usec) / 1000 + (double)(end_t.tv_sec - start_t.tv_sec) * 1000;

    printf("%s   ", inet_ntop(
            sa_recv.sa_family,
            &((struct sockaddr_in6 *) &sa_recv)->sin6_addr,
            ip_strbuf,
            INET6_ADDRSTRLEN
        )
    );

    if (rec_icmp6->icmp6_type == ICMP6_TIME_EXCEEDED && rec_icmp6->icmp6_code == ICMP6_TIME_EXCEED_TRANSIT) {
        printf("%.3f ms\n", diff);
        return 0;
    } else if (rec_icmp6->icmp6_type == ICMP6_DST_UNREACH) {

        switch (rec_icmp6->icmp6_code)
        {
            case ICMP6_DST_UNREACH_NOPORT:
                printf("%.3f ms\n", diff);
                break;
            /*
            case ICMP_UNREACH_HOST:
                printf("H!\n");
                break;
            case ICMP_UNREACH_NET:
                printf("N!\n");
                break;
            */
            case ICMP6_DST_UNREACH_ADMIN:
                printf("X!\n");
                break;
            default:
                printf("?!\n");
                break;
        }
    } else {
        printf("?!\n");
    }

    return 1;
}
