package fi.oph.koski.henkilo

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log.Loggable
import fi.oph.koski.schema.Henkilö

object HenkilöOid {
  def validateHenkilöOid(oid: String): Either[HttpStatus, Henkilö.Oid] = {
    if (Henkilö.isValidHenkilöOid(oid)) {
      Right(oid)
    } else {
      Left(KoskiErrorCategory.badRequest.queryParam.virheellinenHenkilöOid("Virheellinen oid: " + oid + ". Esimerkki oikeasta muodosta: 1.2.246.562.24.00000000001."))
    }
  }
}

trait PossiblyUnverifiedHenkilöOid extends Loggable {
  def oppijaOid: Henkilö.Oid
  def verified: Option[OppijaHenkilö]

  def logString = oppijaOid
}

case class VerifiedHenkilöOid(henkilö: OppijaHenkilö) extends PossiblyUnverifiedHenkilöOid {
  def oppijaOid = henkilö.oid
  override def verified = Some(henkilö)
}

case class UnverifiedHenkilöOid(oppijaOid: Henkilö.Oid, henkilöRepository: HenkilöRepository) extends PossiblyUnverifiedHenkilöOid {
  override lazy val verified: Option[OppijaHenkilö] = henkilöRepository.findByOid(oppijaOid)
}
