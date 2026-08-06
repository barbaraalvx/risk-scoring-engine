#!/usr/bin/env python3
"""
Script de Teste de Carga e Simulação dos Cenários de Fraude (POC 2 - Antifraude Mínimo Viável)
Disciplina: Engenharia de Sistemas Distribuídos (ESD - 2026.1)

Este script realiza testes de estresse e simulação automatizada dos 5 cenários de ataque
previstos na especificação do projeto contra a API do event-ingestion-service.

Cenários contemplados:
  1. BOT (Velocidade de ação): Rajada de requisições em janela curta (ActionVelocityRule).
  2. DEVICE (Fingerprint suspeito): Fingerprints de emuladores/root ou omissos (DeviceFingerprintRule).
  3. MULTI-ACCOUNT (Conluio/Multi-conta): Múltiplos playerIds usando o mesmo IP/Fingerprint (MultiAccountRule).
  4. CHOICE PATTERN (Padrão de escolha): Valores de apostas/saques anômalos (ChoicePatternRule).
  5. STRESS (Carga Concorrente): Múltiplas threads simultâneas medindo RPS, latências P50/P95/P99.
"""

import argparse
import json
import math
import random
import sys
import time
import urllib.error
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime, timezone

# Reconfigura a saída do stdout para UTF-8 em terminais Windows (cp1252)
if hasattr(sys.stdout, "reconfigure"):
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except Exception:
        pass

DEFAULT_URL = "http://localhost:8080/api/v1/actions"

# Visual ANSI colors for terminal output
COLOR_CYAN = "\033[96m"
COLOR_GREEN = "\033[92m"
COLOR_YELLOW = "\033[93m"
COLOR_RED = "\033[91m"
COLOR_BOLD = "\033[1m"
COLOR_RESET = "\033[0m"


def print_banner():
    print(f"{COLOR_CYAN}{COLOR_BOLD}")
    print("==========================================================================")
    print("   [POC 2] MOTOR ANTIFRAUDE: TESTE DE CARGA & SIMULACAO DE ATAQUES        ")
    print("==========================================================================")
    print(f"{COLOR_RESET}")



def current_iso_timestamp():
    return datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")


def send_action(url, payload, timeout=5.0):
    """Envia uma ação via HTTP POST ao event-ingestion-service e mede o tempo de resposta."""
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(
        url,
        data=data,
        headers={"Content-Type": "application/json", "User-Agent": "Antifraude-LoadTester/1.0"},
        method="POST",
    )

    start_time = time.perf_counter()
    try:
        with urllib.request.urlopen(req, timeout=timeout) as response:
            elapsed_ms = (time.perf_counter() - start_time) * 1000.0
            resp_body = json.loads(response.read().decode("utf-8"))
            return {
                "status_code": response.status,
                "success": response.status == 202,
                "latency_ms": elapsed_ms,
                "event_id": resp_body.get("eventId"),
                "error": None,
            }
    except urllib.error.HTTPError as e:
        elapsed_ms = (time.perf_counter() - start_time) * 1000.0
        return {
            "status_code": e.code,
            "success": False,
            "latency_ms": elapsed_ms,
            "event_id": None,
            "error": f"HTTP {e.code}: {e.reason}",
        }
    except Exception as e:
        elapsed_ms = (time.perf_counter() - start_time) * 1000.0
        return {
            "status_code": 0,
            "success": False,
            "latency_ms": elapsed_ms,
            "event_id": None,
            "error": str(e),
        }


def calculate_percentiles(latencies):
    if not latencies:
        return {"min": 0, "mean": 0, "p50": 0, "p90": 0, "p95": 0, "p99": 0, "max": 0}
    sorted_lat = sorted(latencies)
    n = len(sorted_lat)

    def get_p(p):
        idx = max(0, min(n - 1, math.ceil((p / 100.0) * n) - 1))
        return sorted_lat[idx]

    return {
        "min": sorted_lat[0],
        "mean": sum(sorted_lat) / n,
        "p50": get_p(50),
        "p90": get_p(90),
        "p95": get_p(95),
        "p99": get_p(99),
        "max": sorted_lat[-1],
    }


def print_metrics_summary(title, results, duration_sec):
    total = len(results)
    successes = sum(1 for r in results if r["success"])
    failures = total - successes
    latencies = [r["latency_ms"] for r in results]
    stats = calculate_percentiles(latencies)
    rps = total / duration_sec if duration_sec > 0 else 0

    print(f"\n{COLOR_BOLD}[RELATORIO DE METRICAS] {title}{COLOR_RESET}")
    print("-" * 65)
    print(f" Total de Requisicoes:   {total}")
    print(f" Sucessos (202 Accepted): {COLOR_GREEN}{successes}{COLOR_RESET}")
    print(f" Falhas / Erros:         {COLOR_RED if failures > 0 else COLOR_GREEN}{failures}{COLOR_RESET}")
    print(f" Tempo Total:            {duration_sec:.2f} segundos")
    print(f" Vazao (Throughput):     {COLOR_YELLOW}{rps:.2f} req/sec (RPS){COLOR_RESET}")
    print("-" * 65)
    print(f" Latencia Minima:        {stats['min']:.2f} ms")
    print(f" Latencia Media:         {stats['mean']:.2f} ms")
    print(f" Latencia P50 (Mediana): {stats['p50']:.2f} ms")
    print(f" Latencia P90:           {stats['p90']:.2f} ms")
    print(f" Latencia P95:           {COLOR_CYAN}{stats['p95']:.2f} ms{COLOR_RESET}")
    print(f" Latencia P99:           {COLOR_YELLOW}{stats['p99']:.2f} ms{COLOR_RESET}")
    print(f" Latencia Maxima:        {stats['max']:.2f} ms")
    print("=" * 65)


# ==============================================================================
# CENÁRIOS ESPECÍFICOS DE FRAUDE
# ==============================================================================

def run_scenario_bot(url, total_events=30):
    """Cenário 1: Bot (ActionVelocityRule + Multi-Regras). Dispara rajadas rápidas para o mesmo jogador."""
    print(f"\n{COLOR_BOLD}[Cenario 1] SIMULACAO DE BOT - VELOCIDADE DE ACAO E MULTI-REGRAS{COLOR_RESET}")
    print("  Descricao: Envia rajada continua de requisicoes de emulador para um unico playerId.")
    print("  Regras Testadas: ActionVelocityRule + DeviceFingerprintRule + ChoicePatternRule.")

    player_id = "bot_player_99"
    results = []
    start_time = time.perf_counter()

    for i in range(total_events):
        payload = {
            "playerId": player_id,
            "eventType": "WITHDRAWAL",
            "timestamp": current_iso_timestamp(),
            "sessionId": f"sess-bot-{i}",
            "deviceFingerprint": "EMULATOR_ANDROID_ROOT_99",
            "ipAddress": "177.20.10.5",
            "payload": {"amount": 150000.0, "currency": "BRL"}
        }
        res = send_action(url, payload)
        results.append(res)

    duration = time.perf_counter() - start_time
    print_metrics_summary("Cenario 1: Bot Velocity & Multi-Rule Attack", results, duration)
    print(f"INFO: Verifique o status do jogador '{player_id}' via GET http://localhost:8082/quarantine/{player_id}")



def run_scenario_device(url):
    """Cenário 2: Device Fingerprint Suspeito + Valores Atípicos (DeviceFingerprintRule + ChoicePatternRule + Velocity)."""
    print(f"\n{COLOR_BOLD}[Cenario 2] SIMULACAO DE DEVICE SUSPEITO / EMULADOR + APOSATA ATIPICA{COLOR_RESET}")
    print("  Descricao: Envia rajada com emulador/root e saques de alto valor.")
    print("  Regras Testadas: DeviceFingerprintRule + ChoicePatternRule + ActionVelocityRule.")

    suspicious_fingerprints = [
        "EMULATOR_NOX_PLAYER_V7",
        "ANDROID_ROOTED_MAGISK_V26",
        "GENYMOTION_VIRTUAL_DEVICE",
    ]

    results = []
    start_time = time.perf_counter()

    for idx, fp in enumerate(suspicious_fingerprints):
        player_id = f"device_suspect_player_{idx+1}"
        for _ in range(5):
            payload = {
                "playerId": player_id,
                "eventType": "WITHDRAWAL",
                "timestamp": current_iso_timestamp(),
                "sessionId": f"sess-dev-{idx}",
                "deviceFingerprint": fp,
                "ipAddress": "189.40.22.15",
                "payload": {"amount": 80000.0, "currency": "BRL"}
            }
            res = send_action(url, payload)
            results.append(res)
        print(f"  -> Disparado ataque combinado para {player_id} com fingerprint '{fp}' | HTTP {res['status_code']}")

    duration = time.perf_counter() - start_time
    print_metrics_summary("Cenario 2: Device Fingerprint Anomaly", results, duration)


def run_scenario_multiaccount(url):
    """Cenário 3: Multi-Conta / Conluio Coordenado (MultiAccountRule + Device + Velocity)."""
    print(f"\n{COLOR_BOLD}[Cenario 3] SIMULACAO DE MULTI-CONTA E CONLUIO{COLOR_RESET}")
    print("  Descricao: Multiplos playerIds atuando do MESMO IP e MESMO Device Emulador em alta velocidade.")
    print("  Regras Testadas: MultiAccountRule + DeviceFingerprintRule + ActionVelocityRule.")

    shared_ip = "192.168.100.50"
    shared_fp = "EMULATOR_SHARED_DEVICE_FINGERPRINT_XYZ"
    results = []
    start_time = time.perf_counter()

    for i in range(1, 6):
        player_id = f"collusion_player_0{i}"
        for _ in range(5):
            payload = {
                "playerId": player_id,
                "eventType": "ACCOUNT_LINK",
                "timestamp": current_iso_timestamp(),
                "sessionId": f"sess-shared-{i}",
                "deviceFingerprint": shared_fp,
                "ipAddress": shared_ip,
                "payload": {"referredBy": "referral_master", "amount": 50000.0}
            }
            res = send_action(url, payload)
            results.append(res)
        print(f"  -> {player_id} atuando no IP compartilhado {shared_ip} | HTTP {res['status_code']}")

    duration = time.perf_counter() - start_time
    print_metrics_summary("Cenario 3: Multi-Account Collusion", results, duration)


def run_scenario_choicepattern(url):
    """Cenário 4: Padrão de Escolhas / Apostas Atípicas (ChoicePatternRule + Device + Velocity)."""
    print(f"\n{COLOR_BOLD}[Cenario 4] SIMULACAO DE PADRAO DE ESCOLHAS / VALORES ATIPICOS{COLOR_RESET}")
    print("  Descricao: Envia saques e apostas com montantes extremamente desproporcionais a partir de emulador.")
    print("  Regras Testadas: ChoicePatternRule + DeviceFingerprintRule + ActionVelocityRule.")

    player_id = "high_roller_suspect_01"
    results = []
    start_time = time.perf_counter()

    amounts = [50000.00, 100000.00, 250000.00]
    for amt in amounts:
        for _ in range(3):
            payload = {
                "playerId": player_id,
                "eventType": "WITHDRAWAL",
                "timestamp": current_iso_timestamp(),
                "sessionId": "sess-vip-atypical",
                "deviceFingerprint": "EMULATOR_HIGH_ROLLER_DEVICE",
                "ipAddress": "200.150.10.1",
                "payload": {"amount": amt, "currency": "BRL", "destinationPix": "chave-pix-suspeita"}
            }
            res = send_action(url, payload)
            results.append(res)
        print(f"  -> Solicitado saque atipico de R$ {amt:.2f} para {player_id} | HTTP {res['status_code']}")

    duration = time.perf_counter() - start_time
    print_metrics_summary("Cenario 4: Choice Pattern Anomalies", results, duration)



# ==============================================================================
# TESTE DE ESTRESSE E CARGA CONCORRENTE
# ==============================================================================

def generate_random_payload():
    event_types = ["BET", "LOGIN", "WITHDRAWAL", "DEPOSIT", "DEVICE_FINGERPRINT"]
    p_id = f"user_load_{random.randint(1, 100)}"
    e_type = random.choice(event_types)
    return {
        "playerId": p_id,
        "eventType": e_type,
        "timestamp": current_iso_timestamp(),
        "sessionId": f"sess-{random.randint(1000, 9999)}",
        "deviceFingerprint": f"fp-device-{random.randint(1, 20)}",
        "ipAddress": f"192.168.1.{random.randint(1, 254)}",
        "payload": {
            "amount": round(random.uniform(10.0, 500.0), 2),
            "currency": "BRL"
        }
    }


def run_stress_test(url, total_requests, workers):
    print(f"\n{COLOR_BOLD}[Cenario 5] TESTE DE ESTRESSE E CARGA CONCORRENTE{COLOR_RESET}")
    print(f"  Concorrencia: {workers} Threads simultaneas")
    print(f"  Volume Total: {total_requests} Requisicoes HTTP POST")
    print(f"  Alvo:         {url}")
    print("-" * 65)

    results = []
    start_time = time.perf_counter()

    def task_worker(_):
        payload = generate_random_payload()
        return send_action(url, payload)

    with ThreadPoolExecutor(max_workers=workers) as executor:
        futures = [executor.submit(task_worker, i) for i in range(total_requests)]
        completed = 0
        for future in as_completed(futures):
            res = future.result()
            results.append(res)
            completed += 1
            if completed % max(1, total_requests // 10) == 0 or completed == total_requests:
                percent = (completed / total_requests) * 100
                print(f"  Progresso: {completed}/{total_requests} reqs ({percent:.0f}%) concluidas...")

    duration = time.perf_counter() - start_time
    print_metrics_summary(f"Teste de Carga Concorrente ({workers} Workers)", results, duration)


# ==============================================================================
# MAIN ENTRYPOINT
# ==============================================================================

def main():
    print_banner()

    parser = argparse.ArgumentParser(description="Teste de Carga e Simulação de Cenários Antifraude - POC 2")
    parser.add_argument("--url", default=DEFAULT_URL, help=f"URL do endpoint de ingestão (Padrão: {DEFAULT_URL})")
    parser.add_argument(
        "--scenario",
        choices=["all", "bot", "device", "multiaccount", "choicepattern", "stress"],
        default="all",
        help="Cenário de teste a ser executado (Padrão: all)",
    )
    parser.add_argument("--requests", type=int, default=500, help="Total de requisições no teste de estresse (Padrão: 500)")
    parser.add_argument("--workers", type=int, default=20, help="Número de threads simultâneas (Padrão: 20)")

    args = parser.parse_args()

    print(f"Target Endpoint: {COLOR_CYAN}{args.url}{COLOR_RESET}")

    # Checagem inicial de vivacidade (Healthcheck do servidor)
    try:
        health_req = urllib.request.Request(args.url.replace("/actions", "/actuator/health") if "/actions" in args.url else args.url)
        with urllib.request.urlopen(health_req, timeout=2.0) as resp:
            print(f"[OK] Conexão com o serviço confirmada! Status: HTTP {resp.status}\n")
    except Exception:
        print(f"[AVISO] Não foi possível realizar healthcheck em {args.url}. Verifique se a stack Docker está rodando.\n")

    if args.scenario in ["bot", "all"]:
        run_scenario_bot(args.url)
    if args.scenario in ["device", "all"]:
        run_scenario_device(args.url)
    if args.scenario in ["multiaccount", "all"]:
        run_scenario_multiaccount(args.url)
    if args.scenario in ["choicepattern", "all"]:
        run_scenario_choicepattern(args.url)
    if args.scenario in ["stress", "all"]:
        run_stress_test(args.url, args.requests, args.workers)

    print(f"\n{COLOR_GREEN}{COLOR_BOLD}[OK] Execucao dos testes concluida com sucesso!{COLOR_RESET}\n")



if __name__ == "__main__":
    main()
