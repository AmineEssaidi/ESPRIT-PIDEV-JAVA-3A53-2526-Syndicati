param([string]$clientId = "1500782674531455037")

function Send-Frame($pipe, [int]$op, [string]$json) {
    $d = [System.Text.Encoding]::UTF8.GetBytes($json)
    $h = New-Object byte[] 8
    [System.BitConverter]::GetBytes([int]$op).CopyTo($h, 0)
    [System.BitConverter]::GetBytes([int]$d.Length).CopyTo($h, 4)
    $pipe.Write($h, 0, 8)
    $pipe.Write($d, 0, $d.Length)
    $pipe.Flush()
}

function Read-Frame($pipe) {
    $h = New-Object byte[] 8
    [void]$pipe.Read($h, 0, 8)
    $len = [System.BitConverter]::ToInt32($h, 4)
    $d = New-Object byte[] $len
    [void]$pipe.Read($d, 0, $len)
    return [System.Text.Encoding]::UTF8.GetString($d)
}

for ($i = 0; $i -le 9; $i++) {
    $pipeName = "discord-ipc-$i"
    try {
        $pipe = New-Object System.IO.Pipes.NamedPipeClientStream('.', $pipeName, [System.IO.Pipes.PipeDirection]::InOut)
        $pipe.Connect(2000)
        Write-Host "Connected to $pipeName"

        $handshake = '{"v":1,"client_id":"' + $clientId + '"}'
        Send-Frame $pipe 0 $handshake
        Write-Host "Handshake sent"

        $response = Read-Frame $pipe
        Write-Host "Response: $($response.Substring(0, [Math]::Min(200, $response.Length)))"

        if ($response -match "READY") {
            Write-Host "SUCCESS - Discord is ready!"
        } else {
            Write-Host "Got response but no READY"
        }
        $pipe.Close()
        break
    } catch {
        Write-Host "Pipe $i failed: $($_.Exception.Message)"
    }
}
