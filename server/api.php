<?php
	require_once("lib/isas.php");
	
	$isas = new isas($_POST["server"], $_POST["user"], $_POST["pass"]);
	$a = $_GET["action"];
	
	if (!$isas->checkLogin()) {
		die(
			json_encode(
				array(
					"status" => "fail",
					"message" => "Přihlášení se nezdařilo"
				)
			)
		);
	}
	
	$data = ["status" => "ok"];
	
	if ($a == "getmarks") {
		$data["data"] = $isas->GetMarks();
	} else if ($a == "getuser") {
		$data["data"] = [$isas->GetUserInfo()];
	} else if ($a == "getmarkinfo") {
		$data["data"] = [$isas->GetMarkInfo($_POST['id'])];
	}
	
	echo json_encode($data);
	
	$isas->logout();
?>
